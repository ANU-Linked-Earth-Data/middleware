package anuled.dynamicstore;

import org.junit.Test;

import anuled.dynamicstore.TileReader.Pixel;
import junit.framework.TestCase;

/**
 * Test the tile reader on a trivial 32x32 image patch. Test data was created
 * with <code>gdal_translate</code> using data from the NCI.
 */
public class TestTileReader extends TestCase {
	private static int TRUE_WIDTH = 32;
	private static int TRUE_HEIGHT = 32;
	private TileReader reader;
	private TestGeotiff testGT;

	protected void setUp() throws Exception {
		super.setUp();
		
		testGT = new TestGeotiff();
		reader = new TileReader(testGT.getPath());
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		
		testGT.dispose();
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
		assertEquals(numPixels, TRUE_HEIGHT * TRUE_WIDTH);
	}
}
