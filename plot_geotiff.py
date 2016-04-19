#!/usr/bin/env python3

"""Display a LandSat GeoTIFF file as RGB."""

from argparse import ArgumentParser

from osgeo import gdal
import numpy as np
import matplotlib.pyplot as plt


def band_as_array(band):
    int_arr = band.ReadAsArray()
    info = np.iinfo(int_arr.dtype)
    float_arr = int_arr.astype('float') / info.max
    valid_mask = band.GetMaskBand().ReadAsArray().astype('bool')
    float_arr[~valid_mask] = 0
    assert float_arr.min() >= 0.0 and float_arr.max() <= 1.0
    return float_arr


def geotiff2rgb(geotiff):
    # 1, 2, 3 = B, G, R?
    assert geotiff.RasterCount == 6, "Needs to be LandSat-style raster"
    return np.dstack(
        tuple(band_as_array(geotiff.GetRasterBand(n)) for n in [3, 2, 1])
    )

parser = ArgumentParser()
parser.add_argument(
    'geotiff_file', type=str, help='Path to GeoTIFF file to load'
)

if __name__ == '__main__':
    args = parser.parse_args()
    geotiff_fp = gdal.Open(args.geotiff_file)
    rgb = geotiff2rgb(geotiff_fp)
    plt.imshow(rgb)
    plt.show()
