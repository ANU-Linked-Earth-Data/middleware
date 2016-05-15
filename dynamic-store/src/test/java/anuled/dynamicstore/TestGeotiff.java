package anuled.dynamicstore;

import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.gdal.gdal.gdal;

/**
 * Class to encapsulte a test GeoTIFF. Useful for testing Landsat graph, tile
 * reader, etc.
 */
public final class TestGeotiff {
	private static String TEST_MEM_PATH = "/vsimem" + TestData.TEST_GEOTIFF_NAME;
	private boolean disposed;
	
	public TestGeotiff() throws Exception {
		Class<? extends TestGeotiff> cls = this.getClass();
		URL res = cls.getResource(TestData.TEST_GEOTIFF_NAME);
		res.toURI().toURL();
		InputStream stream = res.openStream();
		byte[] testFileBytes = IOUtils.toByteArray(stream);

		gdal.AllRegister();
		gdal.FileFromMemBuffer(TEST_MEM_PATH, testFileBytes);
		
		disposed = false;
	}
	
	public String getPath() {
		assert !disposed;
		return TEST_MEM_PATH;
	}
	
	public void dispose() {
		if (!disposed) {
			gdal.Unlink(TEST_MEM_PATH);
			disposed = true;
		}
	}
}
