package anuled.dynamicstore.backend;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import anuled.dynamicstore.TestData;

public class TestHDF5Dataset {
	private HDF5Dataset ds;
	private static TestData td;

	@BeforeClass
	public static void setUpClass() throws IOException {
		td = new TestData();
	}

	@AfterClass
	public static void tearDownClass() {
		td.dispose();
	}

	@Before
	public void setUp() {
		ds = new HDF5Dataset(td.getPath());
	}

	@After
	public void tearDown() {
		ds.dispose();
	}

	@Test
	public void testInitDatset() {
		assertNotNull(ds);
	}

	@Test
	public void testCellIterator() {
		ds.cells(null, null).forEach(Assert::assertNotNull);
		assertEquals(6, ds.cells(null, null).count());
	}

	@Test
	public void testMetadata() {
		// check dataset metadata is correct
		assertEquals("NBAR", ds.getProdCode());
		assertEquals("LS8", ds.getSatID());
		assertEquals("OLI_TIRS", ds.getSensorID());
		assertEquals(OffsetDateTime.parse("2013-05-27T23:58:20Z"),
				ds.getTimestamp());

		// check cell metadata is correct
		String dggsIdent = "R78520";
		Cell cell = ds.dggsCell(dggsIdent);
		assertNotNull(cell);
		assertEquals(7, cell.getNumBands());
		assertEquals(dggsIdent, cell.getDGGSIdent());
		assertEquals(-34.85536, cell.getLat(), 1e-5);
		assertEquals(149.07407, cell.getLon(), 1e-5);
		// There are ~4 cells around the equator (360 degrees), and we go down 5
		// levels from the 4-cell level, so this cell should span ~360/(4*3^5)
		// degrees (very rough)
		assertEquals(0.37, cell.getDegreesSpanned(), 1e-2);
		// FIXME: the resampler should output -999 or something for invalid
		// values, but right now it's outputting 0 :(
		assertEquals(0, cell.getInvalidValue());
		List<List<Double>> bounds = cell.getBounds();
		// List should be of size 5 and each element should be of size 2
		assertEquals(5, bounds.size());
		assertTrue(bounds.stream().map(l -> l.size() == 2)
				.reduce((l, r) -> l && r).get());
	}
	
	private void isPNG(byte[] data) {
		// In a real programming language, -0x77 would be written 0x89
		byte[] magic = {-0x77, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
		assertTrue(data.length > magic.length);
		assertEquals(magic, Arrays.copyOfRange(data, 0, magic.length));
	}

	@Test
	public void testCell() {
		// Make sure that getting an invalid cell returns null
		Cell cell = ds.dggsCell("R7852999");
		assertNull(cell);

		// Now proceed with a valid cell
		cell = ds.dggsCell("R7852");
		assertNotNull(cell);
		assertEquals("Cell R7852", cell.toString());
		double[] reqPixel = new double[] { 4874.4, 4663.9, 4913.4, 5029.4,
				5192.7, 4063.1, 3048.7 };
		assertArrayEquals(reqPixel, cell.pixelData(), 1e-1);
		byte[] td = cell.tileData(3);
		isPNG(td);
	}

	@Test
	public void testObs() {
		Cell cell = ds.dggsCell("R78520");
		assertNotNull(cell);

		PixelObservation pixelObs = cell.pixelObservation(3);
		double px = pixelObs.getPixel();
		assertTrue(0 < px && px < (1 << 14));
		assertEquals(1 / 0.37, pixelObs.getResolution(), 1e-1);
		assertEquals(6, pixelObs.getPixelLevel());
		assertEquals(6, pixelObs.getCellLevel());

		TileObservation tileObs = cell.tileObservation(4);
		// Just make sure we're getting PNG back (roughly)
		byte[] tile = tileObs.getTile();
		isPNG(tile);
		assertEquals(6, tileObs.getCellLevel());
		assertEquals(8, tileObs.getPixelLevel());
		assertEquals(9 * pixelObs.getResolution(), tileObs.getResolution(),
				1e-1);

		boolean gotException = false;
		try {
			cell.pixelObservation(12);
		} catch (InvalidBandException e) {
			gotException = true;
		}
		assertTrue(gotException);
	}

	@Test
	public void testToString() {
		Cell cell = ds.dggsCell("R78520");
		PixelObservation pixelObs = cell.pixelObservation(3);
		TileObservation tileObs = cell.tileObservation(3);
		String expected = "Observation: band=3, cell=R78520";
		assertEquals(expected + ", type=pixel", pixelObs.toString());
		assertEquals(expected + ", type=tile", tileObs.toString());
	}

	@Test
	public void testEquals() {
		Cell cell = ds.dggsCell("R78520");
		PixelObservation pixelObs = cell.pixelObservation(3);
		TileObservation tileObs = cell.tileObservation(3);

		// Start with some cell checks
		assertTrue(cell.equals(cell));
		assertTrue(cell.equals(ds.dggsCell("R78520")));
		assertTrue(!cell.equals(ds.dggsCell("R7852")));
		// Basic type/identity checks
		assertTrue(pixelObs.equals(pixelObs));
		assertTrue(tileObs.equals(tileObs));
		assertTrue(!pixelObs.equals(tileObs));
		assertTrue(!tileObs.equals(pixelObs));
		// Reconstructing the same observation should work
		TileObservation otherTile = ds.dggsCell("R78520").tileObservation(3);
		assertTrue(tileObs.equals(otherTile));
		// Bands should be equal
		otherTile = ds.dggsCell("R78520").tileObservation(4);
		assertTrue(!tileObs.equals(otherTile));
		// Cell IDs should also be equal
		otherTile = ds.dggsCell("R7852").tileObservation(3);
		assertTrue(!tileObs.equals(otherTile));
	}
}
