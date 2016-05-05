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
	private int     bands;
	private int     pixelWidth;
	private int     pixelHeight;
	/** Affine transform coefficients **/
	private double[] transform;
	
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
	
	public short[] pixel(int row, int col) {
		short[] pixel = new short[bands]; 
		for(int b = 0; b < bands; b ++) {
			pixel[b] = pixelValue(b, row, col);
		}
		return pixel;
	}
	
	public Iterator<short[]> pixels() {
		return new Iterator<short[]>() {
			private int row = 0, col = 0;
			
			@Override
			public short[] next() {
				short[] pixel = pixel(row, col);
				
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
}

