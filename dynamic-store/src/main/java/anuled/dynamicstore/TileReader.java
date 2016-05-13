package anuled.dynamicstore;

// gdal gdal? gdal. gdal gdal gdal gdal!
import org.gdal.gdal.gdal;

import java.util.Iterator;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;

/**
 * Reads in a GeoTIFF file from the AGDC. Includes some utility functions for
 * manipulating it.
 */
public class TileReader {
	private Dataset dataset;
	private int bands;
	public int pixelWidth, pixelHeight;
	/** Affine transform coefficients **/
	private double[] transform;

	public TileReader(String filename) {
		dataset = gdal.Open(filename);
		transform = dataset.GetGeoTransform();
		bands = dataset.getRasterCount();
		pixelWidth = dataset.getRasterXSize();
		pixelHeight = dataset.getRasterYSize();
	}

	private double[] pixelLatLon(int row, int col, double[] latlong) {
		latlong[0] = transform[3] + col * transform[4] + row * transform[5];
		latlong[1] = transform[0] + col * transform[1] + row * transform[2];
		return latlong;
	}

	public double[] pixelLatLon(int row, int col) {
		return pixelLatLon(row, col, new double[2]);
	}

	/** Width of a pixel, in degrees longitude (approx.) */
	public double getPixelWidth() {
		return transform[5];
	}

	/** Height of a pixel, in degrees latitude (approx.) */
	public double getPixelHeight() {
		return transform[1];
	}

	public short pixelValue(int band_no, int row, int col) {
		Band band = dataset.GetRasterBand(band_no);
		short[] retArray = { 0 };
		band.ReadRaster(row, col, 1, 1, retArray);
		return retArray[0];
	}

	public class Pixel {
		public int row, col;
		public short[] pixel;
		public double[] latlong;

		@Override
		public String toString() {
			return "Pixel(" + latlong[0] + "N , " + latlong[1] + "W, "
					+ shortsAsHex(pixel) + ")";
		}

		public Pixel(int row, int col) {
			this.row = row;
			this.col = col;
			this.pixel = TileReader.this.pixel(row, col);
			this.latlong = TileReader.this.pixelLatLon(row, col);
		}
	}

	public short[] pixel(int row, int col) {
		short[] pixel = new short[bands];
		for (int b = 0; b < bands; b++) {
			pixel[b] = pixelValue(b + 1, row, col);
		}
		return pixel;
	}

	/**
	 * Iterable over all pixels in this TileReader
	 */
	public Iterable<Pixel> pixels() {
		return new Iterable<TileReader.Pixel>() {
			@Override
			public Iterator<Pixel> iterator() {
				return new Iterator<Pixel>() {
					private int row = 0, col = 0;

					@Override
					public Pixel next() {
						Pixel pixel = new Pixel(row, col);

						if (row < pixelWidth)
							row++;
						else
							row = 0;
						col++;

						return pixel;
					}

					@Override
					public boolean hasNext() {
						return col < pixelHeight;
					}
				};
			}
		};
	}

	private static String shortsAsHex(short[] thing) {
		StringBuilder builder = new StringBuilder("0x");
		for (short s : thing) {
			builder.append(Integer.toHexString(s & 0xffff));
		}
		return "0x" + builder.toString();
	}

}
