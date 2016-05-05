package anuled.dynamicstore;

// gdal gdal? gdal. gdal gdal gdal gdal!
import org.gdal.gdal.gdal;

import anuled.dynamicstore.TileReader.Pixel;

import java.util.Iterator;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;

/**
 * Reads in a GeoTIFF file from the AGDC. Includes some utility functions for
 * manipulating it.
 */
public class TileReader {
	
	public static void main(String[] args) {
		TileReader tr = new TileReader(args[0]);
		for (Pixel pixel : tr.pixels()) {
			System.out.println(pixel.toString());
		}
	}
	
	public Dataset  dataset;
	public int      bands;
	public int      pixelWidth;
	public int      pixelHeight;
	/** Affine transform coefficients **/
	public double[] transform;
	
	public TileReader(String filename) {
		dataset   = gdal.Open(filename);
		transform = dataset.GetGeoTransform();
		bands = dataset.getRasterCount();
		pixelWidth = dataset.getRasterXSize();
		pixelHeight = dataset.getRasterYSize();
	}
	
	public double[] pixelLatLon(int row, int col, double[] latlong) {
		latlong[0] = transform[3] + col * transform[4] + row * transform[5];
		latlong[1] = transform[0] + col * transform[1] + row * transform[2];
		return latlong;
	}
	
	public double[] pixelLatLon(int row, int col) { 
		return pixelLatLon(row, col, new double[2]);
	}
	
	
	public short pixelValue(int band_no, int row, int col) {
		Band band = dataset.GetRasterBand(band_no);
		short[] retArray = {0};
		band.ReadRaster(row, col, 1, 1, retArray);
		return retArray[0];
	}
	
	public class Pixel {
		public int row, col;
		public short[]  pixel;
		public double[] latlong;
		
		@Override
		public String toString() {
			return "Pixel(" + 
					latlong[0] + "N , " + latlong[1] + "W, " + 
					shortsAsHex(pixel) + ")";
		}
		
		public Pixel(int row, int col) {
			this.row     = row;
			this.col     = col;
			this.pixel   = TileReader.this.pixel(row, col);
			this.latlong = TileReader.this.pixelLatLon(row, col);
		}
	}
	
	public short[] pixel(int row, int col) {
		short[] pixel = new short[bands]; 
		for(int b = 0; b < bands; b ++) {
			pixel[b] = pixelValue(b, row, col);
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
						
						if(row < pixelWidth)
							row ++;
						else
							row = 0;
							col ++;
							
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
	
	public static String shortsAsHex(short[] thing) {
		StringBuilder builder = new StringBuilder("0x");
		for(short s : thing) {
			builder.append(Integer.toHexString(s & 0xffff));
		}
		return "0x" + builder.toString();
	}

}

