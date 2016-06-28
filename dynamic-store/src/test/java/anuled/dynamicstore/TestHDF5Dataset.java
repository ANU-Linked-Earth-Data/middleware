package anuled.dynamicstore;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.time.OffsetDateTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;

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
		ds.cells().forEach(Assert::assertNotNull);
		assertEquals(6, ds.cells().count());
	}

	@Test
	public void testURLs() {
		HDF5Dataset.Cell cell = ds.dggsCell("R7852");
		assertNotNull(cell);

		HDF5Dataset.PixelObservation pxObs = cell.pixelObservation(4);
		assertNotNull(pxObs);
		assertEquals(
				"https://anulinkedearth.org/rdf/observation/2013/05/27/23/58/20/cell/R7852/levelSquare-5/levelPixel-5/band-4",
				URLScheme.observationURL(pxObs));

		HDF5Dataset.TileObservation tlObs = cell.tileObservation(4);
		assertNotNull(tlObs);
		assertEquals(
				"https://anulinkedearth.org/rdf/observation/2013/05/27/23/58/20/cell/R7852/levelSquare-5/levelPixel-7/band-4",
				URLScheme.observationURL(tlObs));
	}
	
	@Test
	public void testMetadata() {
		// check dataset metadata is correct
		assertEquals("NBAR", ds.getProdCode());
		assertEquals("LS8", ds.getSatID());
		assertEquals("OLI_TIRS", ds.getSensorID());
		assertEquals(OffsetDateTime.parse("2013-05-27T23:58:20Z"), ds.getTimestamp());
		
		// check cell metadata is correct
		String dggsIdent = "R78520";
		HDF5Dataset.Cell cell = ds.dggsCell(dggsIdent);
		assertNotNull(cell);
		assertEquals(7, cell.getNumBands());
		assertEquals(dggsIdent, cell.getDGGSIdent());
		assertEquals(-34.85536, cell.getLat(), 1e-5);
		assertEquals(149.07407, cell.getLon(), 1e-5);
	}
}
