#!/usr/bin/env python3

"""Loads a GeoTIFF file representing Landsat data into a triple store via
SPARQL."""

from argparse import ArgumentParser, FileType
from collections import namedtuple
from datetime import datetime
from osgeo import gdal
from rdflib import (ConjunctiveGraph, Literal, Namespace, OWL, RDF, RDFS, XSD,
                    URIRef, BNode)
from urllib.parse import quote_plus
import numpy as np

GEO = Namespace('http://www.w3.org/2003/01/geo/wgs84_pos#')
LS = Namespace('http://example.com/landsat#')
QB = Namespace('http://purl.org/linked-data/cube#')
SDMXD = Namespace('http://purl.org/linked-data/sdmx/2009/dimension#')
SDMXM = Namespace('http://purl.org/linked-data/sdmx/2009/measure#')


def slow(generator, suffix, interval=500, total=None):
    tot_str = '/' + str(total) if total is not None else ''
    for idx, val in enumerate(generator):
        if idx % interval == 0:
            print('{}{} {}'.format(idx, tot_str, suffix))
        yield val


def add_namespaces(rv):
    rv.namespace_manager.bind('geo', GEO)
    rv.namespace_manager.bind('sdmx-dimension', SDMXD)
    rv.namespace_manager.bind('sdmx-measure', SDMXM)
    rv.namespace_manager.bind('qb', QB)
    rv.namespace_manager.bind('landsat', LS)


def pixel_iterator(band):
    """Iterator of (row, col, value) for valid pixels in band."""
    value_arr = band.ReadAsArray()
    valid_mask = band.GetMaskBand().ReadAsArray()
    rows, cols = np.nonzero(valid_mask)
    yield from zip(rows, cols, value_arr[(rows, cols)])


# Use SDMXD.timePeriod and SDMXD.refArea for time and location, respectively.
# LS.sensorValue is just my custom thing
TIME_BLANK = BNode()
AREA_BLANK = BNode()
VALUE_BLANK = BNode()
BOILERPLATE_TRIPLES = [
    (LS.sensorValue, RDF.type, RDF.Property),
    (LS.sensorValue, RDF.type, QB.MeasureProperty),
    (LS.sensorValue, RDFS.label, Literal("sensor reading value")),
    (LS.sensorValue, RDFS.subPropertyOf, SDMXM.obsValue),
    (LS.sensorValue, RDFS.range, XSD.decimal),

    # This datastructure definition is missing a unit component, but I don't
    # care.
    (LS.landsatDSD, RDF.type, QB.DataStructureDefinition),

    (LS.landsatDSD, QB.component, TIME_BLANK),
    (TIME_BLANK, QB.dimension, SDMXD.refTime),
    (TIME_BLANK, QB.order, Literal(1)),

    (LS.landsatDSD, QB.component, AREA_BLANK),
    (AREA_BLANK, QB.dimension, SDMXD.refArea),
    (AREA_BLANK, QB.order, Literal(2)),

    (LS.landsatDSD, QB.component, VALUE_BLANK),
    (VALUE_BLANK, QB.measure, LS.sensorValue),

    (LS.landsatDS, RDF.type, QB.DataSet),
    (LS.landsatDS, RDFS.label, Literal("Landsat sensor data")),
    (LS.landsatDS, RDFS.comment, Literal("Some data from LandSat")),
    (LS.landsatDS, QB.structure, LS.landsatDSD)
]


def undo_transform(row, col, t):
    """Recover (lat, lon) pair from row and column of a GeoTIFF file with
    transform t. See GDAL GetGeoTransform docs for formula."""
    return (
        t[3] + col * t[4] + row * t[5],
        t[0] + col * t[1] + row * t[2]
    )


def build_graph(geotiff):
    """Generator producing all triples which shold go in the Landsat graph"""
    yield from BOILERPLATE_TRIPLES

    band = geotiff.GetRasterBand(1)
    gt = geotiff.GetGeoTransform()

    # Produce one RDF datacube observation per triple
    for row, col, px_val in slow(pixel_iterator(band), 'pixels processed'):
        ident_str = '{}-{}'.format(row, col)
        ident = URIRef(LS['observation-' + ident_str])
        lat, lon = undo_transform(row, col, gt)
        loc_str = '{}, {}'.format(lat, lon)

        # First add data describing the accident
        yield from [
            (ident, RDF.type, QB.Observation),
            (ident, QB.dataSet, LS.landsatDS),
            # TODO: area and time need to be formatted properly
            (ident, SDMXD.refArea, Literal(loc_str)),
            (ident, SDMXD.refTime, BNode()),
            (ident, LS.sensorValue, Literal(px_val)),
        ]

parser = ArgumentParser()
parser.add_argument(
    'geotiff_file', type=str, help='Path to GeoTIFF file to load'
)
parser.add_argument(
    '--query-url', type=str, default='http://localhost:3030/landsat/query',
    dest='query_url', help='Query URL for SPARQL endpoint'
)
parser.add_argument(
    '--update-url', type=str, default='http://localhost:3030/landsat/update',
    dest='update_url', help='Update URL for SPARQL endpoint'
)

if __name__ == '__main__':
    args = parser.parse_args()

    geotiff_fp = gdal.Open(args.geotiff_file)
    graph_triples = build_graph(geotiff_fp)

    fuseki = ConjunctiveGraph(store='SPARQLUpdateStore')
    fuseki.open((args.query_url, args.update_url))

    add_namespaces(fuseki)
    default = 'urn:x-arq:DefaultGraph'
    # TODO: Need to add data in batches. AddN doesn't seem to do a SPARQL
    # update until closing, which is obviously really silly when you have a
    # bajillion pixels.
    fuseki.addN((s, p, o, default) for s, p, o in graph_triples)
    fuseki.close()
