package anuled.dynamicstore.rdfmapper;

import static org.junit.Assert.*;

import java.io.IOException;
import java.time.ZonedDateTime;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import anuled.dynamicstore.TestData;
import anuled.dynamicstore.backend.Cell;
import anuled.dynamicstore.backend.HDF5Dataset;
import anuled.dynamicstore.backend.PixelObservation;
import anuled.dynamicstore.backend.TileObservation;
import anuled.dynamicstore.rdfmapper.URLScheme;

public class TestURLScheme {private HDF5Dataset ds;
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
	public void testParseURL() {
		String[] failureCases = { "https://anulinkedearth.org/rdf/observation/2012/03/",
				"http://google.com/", "!*(malformed(*A" };
		for (String failureCase : failureCases) {
			try {
				URLScheme.parseObservationURL(failureCase);
			} catch (URLScheme.ParseException e) {
				continue;
			}
			assertTrue("String " + failureCase + " should not have parsed",
					false);
		}

		String passCase = "https://anulinkedearth.org/rdf/observation/2012/03/02/23/13/42/cell/R91/"
				+ "levelSquare-3/levelPixel-4/band-5";
		ObservationMeta meta = URLScheme
				.parseObservationURL(passCase);
		assertEquals("R91", meta.cell);
		assertEquals(3, meta.levelSquare);
		assertEquals(4, meta.levelPixel);
		assertEquals(5, meta.band);
		assertEquals(ZonedDateTime.parse("2012-03-02T23:13:42Z"),
				meta.timestamp);
	}

	@Test
	public void testURLs() {
		Cell cell = ds.dggsCell("R7852");
		assertNotNull(cell);

		PixelObservation pxObs = cell.pixelObservation(4);
		assertNotNull(pxObs);
		assertEquals(
				"https://anulinkedearth.org/rdf/observation/2013/05/27/23/58/20/cell/R7852/levelSquare-5/levelPixel-5/band-4",
				URLScheme.observationURL(pxObs));

		TileObservation tlObs = cell.tileObservation(4);
		assertNotNull(tlObs);
		assertEquals(
				"https://anulinkedearth.org/rdf/observation/2013/05/27/23/58/20/cell/R7852/levelSquare-5/levelPixel-7/band-4",
				URLScheme.observationURL(tlObs));
	}
}
