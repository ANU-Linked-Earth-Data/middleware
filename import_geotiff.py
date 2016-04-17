#!/usr/bin/env python3

"""Loads a GeoTIFF file representing Landsat data into a triple store via
SPARQL."""

from argparse import ArgumentParser, FileType
from collections import namedtuple
from datetime import datetime
from io import StringIO
from itertools import islice, chain
import logging
import re
from urllib.parse import quote_plus

from osgeo import gdal
import numpy as np
import pytz
from screw_rdflib import (ConjunctiveGraph, Literal, Namespace, OWL, RDF, RDFS,
                          XSD, URIRef, BNode, Graph)

# RDF namespaces
GEO = Namespace('http://www.w3.org/2003/01/geo/wgs84_pos#')
LS = Namespace('http://example.com/landsat#')
QB = Namespace('http://purl.org/linked-data/cube#')
SDMXC = Namespace('http://purl.org/linked-data/sdmx/2009/concept#')
SDMXD = Namespace('http://purl.org/linked-data/sdmx/2009/dimension#')
SDMXM = Namespace('http://purl.org/linked-data/sdmx/2009/measure#')
# For parsing AGDC filenames
AGDC_RE = re.compile(
    r'^(?P<sat_id>[^_]+)_(?P<sensor_id>[^_]+)_(?P<prod_code>[^_]+)_'
    r'(?P<lon>[^_]+)_(?P<lat>[^_]+)_(?P<year>\d{4})-(?P<month>\d{2})-'
    r'(?P<day>\d{2})T(?P<hour>\d+)-(?P<minute>\d+)-(?P<second>\d+(\.\d+)?)'
    r'\.tif$'
)
# To select default graph in Jena
DEFAULT = 'urn:x-arq:DefaultGraph'
# Boilerplate turtles are the best turtles
BOILERPLATE_TURTLE = """
@prefix : <{LS}> .
@prefix rdf: <{RDF}> .
@prefix rdfs: <{RDFS}> .
@prefix xsd: <{XSD}> .
@prefix qb: <{QB}> .
@prefix sdmx-concept: <{SDMXC}> .
@prefix sdmx-dimension: <{SDMXD}> .
@prefix sdmx-measure: <{SDMXM}> .
@prefix geo: <{GEO}> .

:refTime a rdf:Property , qb:DimensionProperty;
    rdfs:label "time of observation"@en;
    rdfs:subPropertyOf sdmx-dimension:refPeriod;
    rdfs:range xsd:dateTime;
    qb:concept sdmx-concept:refPeriod .

:refArea a rdf:Property , qb:DimensionProperty;
    rdfs:label "point of observation"@en;
    rdfs:subPropertyOf sdmx-dimension:refArea;
    rdfs:range geo:SpatialThing;
    qb:concept sdmx-concept:refArea .

:sensorValue a rdf:Property , qb:MeasureProperty ;
    rdfs:label "sensor reading value"@en ;
    rdfs:subPropertyOf sdmx-measure:obsValue ;
    rdfs:range xsd:integer .

:landsatDSD a qb:DataStructureDefinition ;
    qb:component [
        qb:dimension :refTime ;
        qb:order 1
    ] ;
    qb:component [
        qb:dimension sdmx-dimension:refArea ;
        qb:order 2
    ] ;
    qb:component [
        qb:measure :sensorValue ;
    ] .

:landsatDS a qb:DataSet ;
    rdfs:label "Landsat sensor data"@en ;
    rdfs:comment "Some data from LandSat, retrieved from AGDC"@en ;
    qb:structure :landsatDSD .
""".format(QB=QB, SDMXD=SDMXD, SDMXM=SDMXM, LS=LS, GEO=GEO, SDMXC=SDMXC,
           RDF=RDF, RDFS=RDFS, XSD=XSD)


def parse_agdc_fn(fn):
    """Parses a filename in the format used by the AGDC Landsat archive. For
    example, ``LS7_ETM_NBAR_149_-036_2012-02-10T23-50-47.650696.tif`` is a
    Landsat 7 observation in GeoTIFF format taken on Februrary 10 2012 at
    11:50pm (amongst other things).

    >>> fn = 'LS7_ETM_NBAR_149_-036_2012-02-10T23-50-47.650696.tif'
    >>> sorted_results = sorted(parse_agdc_fn(fn).items())
    >>> print('\\n'.join('{}: {}'.format(k, v) for k, v in sorted_results))
    datetime: 2012-02-10 23:50:47.650696+00:00
    lat: -36.0
    lon: 149.0
    prod_code: NBAR
    sat_id: LS7
    sensor_id: ETM

    :param string fn: Filename for observation.
    :return: Dictionary of extracted metdata from filename.
    """
    match = AGDC_RE.match(fn)
    if match is None:
        raise ValueError('Invalid AGDC filename: "{}"'.format(fn))
    info = match.groupdict()
    raw_sec = float(info['second'])
    int_sec = int(raw_sec)
    microsecond = int(1e6 * (raw_sec % 1))
    dt = pytz.datetime.datetime(
        year=int(info['year']), month=int(info['month']), day=int(info['day']),
        hour=int(info['hour']), minute=int(info['minute']),
        second=int_sec, microsecond=microsecond, tzinfo=pytz.utc
    )
    return {
        'lat': float(info['lat']), 'lon': float(info['lon']), 'datetime': dt,
        'prod_code': info['prod_code'], 'sensor_id': info['sensor_id'],
        'sat_id': info['sat_id']
    }


def slow(generator, suffix, interval=500, total=None):
    """Used to annotate slow generators. Will print progress every ``interval``
    yields."""
    tot_str = '/' + str(total) if total is not None else ''
    for idx, val in enumerate(generator):
        if idx % interval == 0:
            print('{}{} {}'.format(idx, tot_str, suffix))
        yield val


def pixel_iterator(band):
    """Iterator of (row, col, value) for valid pixels in band."""
    value_arr = band.ReadAsArray()
    valid_mask = band.GetMaskBand().ReadAsArray()
    rows, cols = np.nonzero(valid_mask)
    yield from zip(rows, cols, value_arr[(rows, cols)])


def undo_transform(row, col, t):
    """Recover (lat, lon) pair from row and column of a GeoTIFF file with
    transform t. See GDAL GetGeoTransform docs for formula."""
    return (
        t[3] + col * t[4] + row * t[5],
        t[0] + col * t[1] + row * t[2]
    )


def build_graph(geotiff):
    """Generator producing all observation triples."""
    boilerplate_graph = Graph().parse(
        StringIO(BOILERPLATE_TURTLE), format='turtle'
    )
    yield from boilerplate_graph.triples((None, None, None))

    band = geotiff.GetRasterBand(1)
    gt = geotiff.GetGeoTransform()

    filename, = geotiff.GetFileList()
    gt_meta = parse_agdc_fn(filename)

    # Produce one RDF datacube observation per triple
    for row, col, px_val in slow(pixel_iterator(band), 'pixels processed'):
        ident_str = '{}-{}'.format(row, col)
        ident = URIRef(LS['observation-' + ident_str])
        lat, lon = undo_transform(row, col, gt)
        loc_bnode = BNode()

        # First add data describing the accident
        yield from [
            (ident, RDF.type, QB.Observation),
            (ident, QB.dataSet, LS.landsatDS),
            (ident, SDMXD.refArea, loc_bnode),
            (loc_bnode, RDF.type, GEO.Point),
            (loc_bnode, GEO.lat, Literal(lat, datatype=XSD.decimal)),
            (loc_bnode, GEO.lon, Literal(lon, datatype=XSD.decimal)),
            (ident, LS.refTime, Literal(gt_meta['datetime'])),
            (ident, LS.sensorValue, Literal(px_val, datatype=XSD.integer)),
        ]


def iterchunk(iterator, n):
    """Chunk iterator into blocks of size n"""
    it = iter(iterator)
    while True:
        chunk = islice(it, n)
        try:
            fst = next(chunk)
        except StopIteration:
            return
        yield chain([fst], chunk)


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

    # Batch the triples so that Python doesn't asplode
    for triples in slow(iterchunk(graph_triples, 10000), 'chunks'):
        fuseki = ConjunctiveGraph(store='SPARQLUpdateStore')
        fuseki.open((args.query_url, args.update_url))
        fuseki.addN((s, p, o, DEFAULT) for s, p, o in triples)
        fuseki.close()

    print('Done')
