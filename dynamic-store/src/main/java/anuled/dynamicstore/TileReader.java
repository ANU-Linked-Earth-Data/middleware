package anuled.dynamicstore;

// gdal gdal? gdal. gdal gdal gdal gdal!
import org.gdal.gdal.gdal;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
/**
 * Reads in a GeoTIFF file from the AGDC. Includes some utility functions for
 * manipulating it.
 */
public class TileReader {
	private Dataset dataset;
	private double[] transform;
	
	public TileReader(String filename) {
		dataset = gdal.Open(filename);
		transform = dataset.GetGeoTransform();
	}
	
	public double[] pixelLatLon(int row, int col) {
		double lat = transform[3] + col * transform[4] + row * transform[5];
		double lon = transform[0] + col * transform[1] + row * transform[2];
		return new double[]{lat, lon};
	}
	
	public int pixelWidth() {
		return dataset.getRasterXSize();
	}
	
	public int pixelHeight() {
		return dataset.getRasterYSize();
	}
	
	public int numBands() {
		return dataset.getRasterCount();
	}
	
	public short pixelValue(int band_no, int row, int col) {
		Band band = dataset.GetRasterBand(band_no);
		short[] retArray = {0};
		band.ReadRaster(row, col, 1, 1, retArray);
		return retArray[0];
	}
}

