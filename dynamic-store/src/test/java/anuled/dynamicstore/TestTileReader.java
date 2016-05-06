package anuled.dynamicstore;

import java.io.InputStream;
import java.net.URL;

import org.gdal.gdal.gdal;
import org.junit.Test;

import anuled.dynamicstore.TileReader.Pixel;

import org.apache.commons.io.IOUtils;

import junit.framework.TestCase;

/**
 * Test the tile reader on a trivial 32x32 image patch. Test data was created
 * with <code>gdal_translate</code> using data from the NCI.
 */
public class TestTileReader extends TestCase {
	private static String TEST_FILE_NAME = "/LS7_ETM_NBAR_149_-035_2013-02-21T23-46-15.883624.small.tif";
	private static String TEST_MEM_PATH = "/vsimem" + TEST_FILE_NAME;
	private static int TRUE_WIDTH = 32;
	private static int TRUE_HEIGHT = 32;
	private TileReader reader;

	protected void setUp() throws Exception {
		super.setUp();
		
		Class<? extends TestTileReader> cls = this.getClass();
		URL res = cls.getResource(TEST_FILE_NAME);
		res.toURI().toURL();
		InputStream stream = res.openStream();
		byte[] testFileBytes = IOUtils.toByteArray(stream);

		gdal.AllRegister();
		gdal.FileFromMemBuffer(TEST_MEM_PATH, testFileBytes);
		
		reader = new TileReader(TEST_MEM_PATH);
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		gdal.Unlink(TEST_MEM_PATH);
	}
	
	@Test
	public void testSize() {
		assertEquals(reader.pixelHeight, TRUE_HEIGHT);
		assertEquals(reader.pixelWidth, TRUE_WIDTH);
	}
	
	@Test
	public void testPixelIterator() {
		Iterable<Pixel> iter = reader.pixels();
		int numPixels = 0;
		for (@SuppressWarnings("unused") Pixel pixel : iter) {
			numPixels += 1;
		}
		assert(numPixels == TRUE_HEIGHT * TRUE_WIDTH);
	}
}
