#!/usr/bin/env python3

"""Loads an HDF5 file representing Landsat data into a triple store via
SPARQL."""

from argparse import ArgumentParser
from base64 import b64encode
from io import StringIO, BytesIO
from itertools import islice, chain

import h5py
import numpy as np
from scipy.misc import toimage
from screw_rdflib import (ConjunctiveGraph, Literal, Namespace, OWL, RDF, RDFS,
                          XSD, URIRef, BNode, Graph)

# RDF namespaces
GEO = Namespace('http://www.w3.org/2003/01/geo/wgs84_pos#')
LED = Namespace('http://www.example.org/ANU-LED#')
QB = Namespace('http://purl.org/linked-data/cube#')
SDMXC = Namespace('http://purl.org/linked-data/sdmx/2009/concept#')
SDMXD = Namespace('http://purl.org/linked-data/sdmx/2009/dimension#')
SDMXM = Namespace('http://purl.org/linked-data/sdmx/2009/measure#')
OGC = Namespace('http://www.opengis.net/ont/geosparql#')
# Default graph to update (doesn't really matter because default graph on query
# is union of all named graphs)
DEFAULT = LED.lsGraph
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
               , :etmBandComponent
               , :dggsComponent
               , :dggsCellComponent
               , :dggsLevelSquareComponent
               , :dggsLevelPixelComponent .

:landsatDS a qb:DataSet ;
    rdfs:label "Landsat sensor data"@en ;
    rdfs:comment "Some data from LandSat, retrieved from AGDC"@en ;
    qb:structure :landsatDSD ;
    :instrument gcmd-instrument:SCANNER ;
    :satellite gcmd-platform:LANDSAT-7 ;
    :dggs "rHEALPix WGS84 Ellipsoid" .

:instrumentComponent a qb:ComponentSpecification ;
    qb:attribute :instrument .

:positionComponent a qb:ComponentSpecification ;
    qb:dimension :location .

:satelliteComponent a qb:ComponentSpecification ;
    qb:attribute :satellite .

:timeComponent a qb:ComponentSpecification ;
    qb:dimension :time .

:dataComponent a qb:ComponentSpecification ;
    qb:measure :imageData .

:etmBandComponnet a qb:ComponentSpecification ;
    qb:dimension :etmBand .

:dggsComponent a qb:ComponentSpecification ;
    qb:attribute :dggs .

:dggsCellComponent a qb:ComponentSpecification ;
    qb:dimension :dggsCell .

:dggsLevelSquareComponent a qb:ComponentSpecification ;
    qb:dimension :dggsLevelSquare .

:dggsLevelPixelComponent a qb:ComponentSpecification ;
    qb:dimension :dggsLevelPixel .

:etmBand a qb:AttributeProperty ;
    rdfs:label "LandSat ETM observation band"@en;
    rdfs:range xsd:integer .

:instrument a qb:AttributeProperty ;
    rdfs:range gcmd-instrument:Instrument .

:satellite a qb:AttributeProperty ;
    rdfs:range gcmd-platform:PLATFORM .

:time a qb:AttributeProperty ;
    rdfs:range xsd:dateTime .

:dggs a qb:AttributeProperty ;
    rdfs:range xsd:string .

:dggsCell a owl:DatatypeProperty, qb:DimensionProperty ;
    rdfs:range xsd:string .

:dggsLevelSquare a qb:DimensionProperty ;
    rdfs:range xsd:integer .

:dggsLevelPixel a qb:DimensionProperty ;
    rdfs:range xsd:integer .
""".format(QB=QB, SDMXD=SDMXD, SDMXM=SDMXM, LED=LED, GEO=GEO, SDMXC=SDMXC,
           RDF=RDF, RDFS=RDFS, XSD=XSD, OWL=OWL, OGC=OGC)


def slow(generator, suffix, interval=500, total=None):
    """Used to annotate slow generators. Will print progress every ``interval``
    yields."""
    tot_str = '/' + str(total) if total is not None else ''
    for idx, val in enumerate(generator):
        if idx % interval == 0:
            print('{}{} {}'.format(idx, tot_str, suffix))
        yield val


def array_to_png(array):
    """Turn a 2D array into a data: URI filled with PNG goodies :)"""
    assert array.ndim == 2

    # Convert to PIL image with transparent pixels for masked values
    im = toimage(array)
    mask = array.mask
    if mask.shape:
        # Only bother putting in an alpha channel if there are masked values
        alpha = toimage(~mask)
        im.putalpha(alpha)
    else:
        assert not mask, 'Need to have some unmasked values'

    # Now save to base64-encoded data: URL
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


def cell_level_square(cell_id):
    """Get level in DGGS hierarchy associated with slash-separated cell ID.
    Maps `/R/0/0/0/0/5` to 5, for instance"""
    return len([x for x in cell_id.split('/') if x])


def graph_for_data(cell_id, tile, meta):
    is_pixel = tile.ndim <= 1
    if is_pixel:
        tile_size = 1
    else:
        tile_w, tile_h = tile.shape
        assert tile_w == tile_h
        tile_size = tile_w

    # Find level in DGGS hierarchy of current square and current data
    level_square = cell_level_square(cell_id)
    if is_pixel:
        level_pixel = level_square
    else:
        extra = np.log(tile_size) / np.log(3)
        int_extra = int(round(extra))
        assert abs(extra - int_extra) < 1e-5, \
            'Tile size needs to be power of 3'
        level_pixel = level_square + int_extra

    ident_str = 'cell/' + cell_id + '/pixelLevel/' + str(level_pixel)
    ident = URIRef(LED['observation/' + ident_str])

    # Bounding box for the tile, which we'll convert into WKT
    bbox_corners = meta['bounds']
    loc_wkt = Literal('POLYGON(({0}, {1}, {2}, {3}, {0}))'.format(
        *['{} {}'.format(lon, lat) for lon, lat in bbox_corners]
    ), datatype=OGC.wktLiteral)

    # Woooo this resolution calculation makes no sense
    maxes = bbox_corners.max(axis=0)
    mins = bbox_corners.min(axis=0)
    res = np.mean(tile_size / np.abs(maxes - mins))

    if is_pixel:
        yield from [
            (ident, LED.imageData, Literal(float(tile))),
            (ident, RDF.type, LED.Pixel)
        ]
    else:
        png_tile = URIRef(array_to_png(tile))
        yield from [
            (ident, LED.imageData, png_tile),
            (ident, RDF.type, LED.GridSquare)
        ]

    # Actual data
    yield from [
        (ident, RDF.type, QB.Observation),
        (ident, QB.dataSet, LED.landsatDS),
        (ident, LED.bounds, loc_wkt),
        # TODO: Need to get real band number in here. Blocked on upgrade of
        # resampler to do that.
        (ident, LED.etmBand, Literal(1, datatype=XSD.integer)),
        (ident, LED.time, Literal(meta['datetime'], datatype=XSD.datetime)),
        (ident, LED.resolution,
            Literal(res, datatype=XSD.decimal)),
        (ident, LED.dggsCell, Literal(cell_id)),
        (ident, LED.dggsLevelSquare, Literal(level_square)),
        (ident, LED.dggsLevelPixel, Literal(level_pixel))
    ]

    # Yield the centre point
    centre_lon, centre_lat = meta['centre']
    yield from loc_triples(ident, LED.location, centre_lat, centre_lon)


def graph_for_cell(cell, band):
    """Process a single DGGS cell, represented as a h5py group."""
    # [()] converts to Numpy array
    pixel = cell['pixel'][()]
    data = cell['data'][()]
    assert pixel.shape == data.shape[::-1][2:], \
        'Pixel and tile need same channel count'
    if pixel.size > 1:
        assert band < pixel.size, 'Band must be in range'
        pixel = pixel[band]
        data = data[band, :, :]
    else:
        assert band == 0, 'Only one band available'
    masked_data = np.ma.masked_values(data, cell.attrs['missing_value'])
    meta = dict(cell.attrs)
    cell_id = cell.name

    # Both pixel and dense data are treated as "data" (just one has a
    # resolution of 1x1)
    yield from graph_for_data(cell_id, pixel, meta)
    yield from graph_for_data(cell_id, masked_data, meta)


def data_cell_ids(hdf5_file):
    """Get list of paths (e.g. ``/R/7/8/5/2/1``) pointing to HDF5 groups
    containing actual data."""
    to_explore = ['/' + k for k in hdf5_file.keys()]
    while to_explore:
        top = to_explore.pop()
        group = hdf5_file[top]
        if 'data' in group:
            assert 'pixel' in group, \
                'HDF5 groups with `data` members need `pixel` members too'
            yield top

        # Now add its children
        children = [top + '/' + k for k in group.keys()
                    if isinstance(group[k], h5py.Group)]
        to_explore.extend(children)


def build_graph(hdf5_file, band):
    """Generator producing all observation triples for each band."""
    boilerplate_graph = Graph().parse(
        StringIO(BOILERPLATE_TURTLE), format='turtle'
    )
    yield from boilerplate_graph.triples((None, None, None))

    # Just throw the data in the HDF5 file straight into our graph. There's no
    # need to do fancy slicing like we did for GeoTIFF.
    for cell_id in data_cell_ids(hdf5_file):
        group = hdf5_file[cell_id]
        print('Processing cell {}'.format(cell_id))
        yield from graph_for_cell(group, band)


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
    'hdf5_file', type=str, help='Path to DGGS-formatted HDF5 file to load'
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
    '--band', type=int, default=0, dest='band',
    help='Which band to use (if there are several)'
)

if __name__ == '__main__':
    args = parser.parse_args()

    with h5py.File(args.hdf5_file, 'r') as hdf5_fp:
        graph_triples = build_graph(hdf5_fp, args.band)

        # Batch the triples so that Python doesn't asplode
        for triples in slow(iterchunk(graph_triples, 100), 'chunks'):
            fuseki = ConjunctiveGraph(store='SPARQLUpdateStore')
            fuseki.open((args.query_url, args.update_url))
            fuseki.addN((s, p, o, DEFAULT) for s, p, o in triples)
            fuseki.close()

    print('Done')
