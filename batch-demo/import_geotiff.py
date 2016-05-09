#!/usr/bin/env python3

"""Loads a GeoTIFF file representing Landsat data into a triple store via
SPARQL."""

from argparse import ArgumentParser, FileType
from base64 import b64encode
from collections import namedtuple
from datetime import datetime
from io import StringIO, BytesIO
from itertools import islice, chain
import logging
import re
from urllib.parse import quote_plus

from osgeo import gdal
import numpy as np
import pytz
from scipy.misc import toimage
from screw_rdflib import (ConjunctiveGraph, Literal, Namespace, OWL, RDF, RDFS,
                          XSD, URIRef, BNode, Graph, OWL)

# RDF namespaces
GEO = Namespace('http://www.w3.org/2003/01/geo/wgs84_pos#')
LED = Namespace('http://www.example.org/ANU-LED#')
QB = Namespace('http://purl.org/linked-data/cube#')
SDMXC = Namespace('http://purl.org/linked-data/sdmx/2009/concept#')
SDMXD = Namespace('http://purl.org/linked-data/sdmx/2009/dimension#')
SDMXM = Namespace('http://purl.org/linked-data/sdmx/2009/measure#')
OGC = Namespace('http://www.opengis.net/ont/geosparql#')
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
@prefix : <{LED}> .
@prefix rdf: <{RDF}> .
@prefix rdfs: <{RDFS}> .
@prefix xsd: <{XSD}> .
@prefix qb: <{QB}> .
@prefix sdmx-concept: <{SDMXC}> .
@prefix sdmx-dimension: <{SDMXD}> .
@prefix sdmx-measure: <{SDMXM}> .
@prefix geo: <{GEO}> .
@prefix owl: <{OWL}> .
@prefix ogc: <{OGC}> .
@prefix gcmd-platform: <http://geobrain.laits.gmu.edu/ontology/2004/11/gcmd-platform.owl#> .
@prefix gcmd-instrument: <http://geobrain.laits.gmu.edu/ontology/2004/11/gcmd-instrument.owl#> .

:landsatDSD a qb:DataStructureDefinition ;
    qb:component :instrumentComponent
               , :positionComponent
               , :satelliteComponent
               , :timeComponent
               , :dataComponent
               , :etmBandComponent .

:landsatDS a qb:DataSet ;
    rdfs:label "Landsat sensor data"@en ;
    rdfs:comment "Some data from LandSat, retrieved from AGDC"@en ;
    qb:structure :landsatDSD ;
    :instrument gcmd-instrument:SCANNER ;
    :satellite gcmd-platform:LANDSAT-7 .

:instrumentComponent a qb:ComponentSpecification ;
    qb:attribute :instrument .

:positionComponent a qb:ComponentSpecification ;
    qb:dimension :location .

:satelliteComponent a qb:ComponentSpecification ;
    qb:attribute :satellite .

:timeComponent a qb:ComponentSpecification ;
    qb:dimension :time .

:dataComponent a qb:ComponentSpecification ;
    qb:dimension :imageData .

:etmBandComponnet a qb:ComponentSpecification ;
    qb:dimension :etmBand .

:etmBand a qb:AttributeProperty ;
    rdfs:label "LandSat ETM observation band"@en;
    rdfs:range xsd:integer .

:instrument a qb:AttributeProperty ;
    rdfs:range gcmd-instrument:Instrument .

:satellite a qb:AttributeProperty ;
    rdfs:range gcmd-platform:PLATFORM .

:time a qb:AttributeProperty ;
    rdfs:range xsd:dateTime .
""".format(QB=QB, SDMXD=SDMXD, SDMXM=SDMXM, LED=LED, GEO=GEO, SDMXC=SDMXC,
           RDF=RDF, RDFS=RDFS, XSD=XSD, OWL=OWL, OGC=OGC)


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


def tile_iterator(band, tile_size):
    """Iterator of (top left row, top left col, data) for valid tiles in band
    (starting at left side, rather than using some more sophisticated
    scheme)."""
    value_arr = band.ReadAsArray()
    valid_mask = band.GetMaskBand().ReadAsArray().astype('bool')
    for row in range(0, value_arr.shape[0], tile_size):
        for col in range(0, value_arr.shape[1], tile_size):
            if valid_mask[row:row+tile_size, col:col+tile_size].any():
                vals = value_arr[row:row+tile_size, col:col+tile_size]
                yield (row, col, vals)



def undo_transform(row, col, t):
    """Recover (lat, lon) pair from row and column of a GeoTIFF file with
    transform t. See GDAL GetGeoTransform docs for formula."""
    return (
        t[3] + col * t[4] + row * t[5],
        t[0] + col * t[1] + row * t[2]
    )


def array_to_png(array):
    """Turn a 2D array into a data: URI filled with PNG goodies :)"""
    assert array.ndim == 2
    im = toimage(array)
    fp = BytesIO()
    im.save(fp, format='png')
    data = b64encode(fp.getvalue()).decode('utf-8')
    return 'data:image/png;base64,' + data


def loc_triples(subj, prop, lat, lon):
    """Yield a bunch of triples indicating that something is at a given
    latitude and longitude. This is actually really painful because of
    blank nodes :("""
    loc_bnode = BNode()
    yield (subj, prop, loc_bnode)
    yield (loc_bnode, GEO.lat, Literal(lat, datatype=XSD.decimal))
    yield (loc_bnode, GEO.lon, Literal(lon, datatype=XSD.decimal))


def graph_for_band(band, band_num, tile_size, gt_meta, transform):
    """Process a single band in a GeoTIFF file."""
    it = tile_iterator(band, tile_size)
    for row, col, tile in slow(it, 'tiles processed', interval=10):
        start_lat, start_lon = undo_transform(row, col, transform)
        end_lat, end_lon = undo_transform(
            row + tile_size, col + tile_size, transform
        )
        # Bounding box for the tile, which we'll convert into WKT
        bbox_corners = [
            (start_lon, start_lat), (start_lon, end_lat),
            (end_lon, end_lat), (end_lon, start_lat)
        ]
        loc_wkt = Literal('POLYGON({0}, {1}, {2}, {3}, {0})'.format(
            *['{} {}'.format(lon, lat) for lat, lon in bbox_corners]
        ), datatype=OGC.wktLiteral)
        png_tile = URIRef(array_to_png(tile))
        ident_str = 'lat/{}/lon/{}/tile-size/{}/band/{}'.format(
            start_lat, start_lon, tile_size, band_num
        )
        ident = URIRef(LED['observation/' + ident_str])
        # Woooo this makes no sense
        res = np.mean(np.abs([
            tile.shape[1] / (start_lat - end_lat),
            tile.shape[0] / (start_lon - end_lon)
        ]))

        # First add data describing the accident
        yield from [
            (ident, RDF.type, QB.Observation),
            (ident, QB.dataSet, LED.landsatDS),
            (ident, LED.bounds, loc_wkt),
            (ident, LED.etmBand, Literal(band_num, datatype=XSD.integer)),
            (ident, LED.time, Literal(gt_meta['datetime'])),
            (ident, LED.imageData, png_tile),
            (ident, LED.resolution,
                Literal(res, datatype=XSD.decimal))
        ]

        # Yield the centre point
        yield from loc_triples(
            ident, LED.location,
            (end_lat + start_lat) / 2,
            (end_lon + start_lon) / 2
        )


def build_graph(geotiff, tile_sizes):
    """Generator producing all observation triples for each band."""
    boilerplate_graph = Graph().parse(
        StringIO(BOILERPLATE_TURTLE), format='turtle'
    )
    yield from boilerplate_graph.triples((None, None, None))

    gt = geotiff.GetGeoTransform()
    filename, = geotiff.GetFileList()
    gt_meta = parse_agdc_fn(filename)

    for band_num in range(1, geotiff.RasterCount + 1):
        band = geotiff.GetRasterBand(band_num)
        for tile_size in tile_sizes:
            print('Processing band {}/{}, tile size {}'.format(
                band_num, geotiff.RasterCount, tile_size
            ))
            yield from graph_for_band(band, band_num, tile_size, gt_meta, gt)


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
parser.add_argument(
    '--tile-sizes', type=int, default=[64, 128], nargs='+',
    dest='tile_sizes', help='Sizes of tiles to output'
)

if __name__ == '__main__':
    args = parser.parse_args()

    geotiff_fp = gdal.Open(args.geotiff_file)
    graph_triples = build_graph(geotiff_fp, args.tile_sizes)

    # Batch the triples so that Python doesn't asplode
    for triples in slow(iterchunk(graph_triples, 100), 'chunks'):
        fuseki = ConjunctiveGraph(store='SPARQLUpdateStore')
        fuseki.open((args.query_url, args.update_url))
        fuseki.addN((s, p, o, DEFAULT) for s, p, o in triples)
        fuseki.close()

    print('Done')
