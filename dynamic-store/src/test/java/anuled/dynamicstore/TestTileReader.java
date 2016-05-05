package anuled.dynamicstore;

import java.net.URL;
import java.util.Enumeration;

import org.junit.Test;

import junit.framework.TestCase;

/**
 * Test the tile reader on a trivial 32x32 image patch. Test data was created
 * with <code>gdal_translate</code> using data from the NCI.
 */
public class TestTileReader extends TestCase {
	private static String TEST_FILE_NAME = "/LS7_ETM_NBAR_149_-035_2013-02-21T23-46-15.883624.small.tif";
	private TileReader reader;

	protected void setUp() throws Exception {
		super.setUp();
		Class<? extends TestTileReader> cls = this.getClass();
		URL res = cls.getResource(TEST_FILE_NAME);
		String filePath = res.toURI().toString();
		reader = new TileReader(filePath);
	}
	
	@Test
	public void testSize() {
		assertEquals(reader.pixelHeight, 32);
		assertEquals(reader.pixelWidth, 32);
	}
}
