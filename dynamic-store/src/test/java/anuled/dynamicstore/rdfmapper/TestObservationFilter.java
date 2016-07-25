package anuled.dynamicstore.rdfmapper;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import anuled.dynamicstore.TestData;
import anuled.dynamicstore.backend.Cell;
import anuled.dynamicstore.backend.HDF5Dataset;
import anuled.dynamicstore.backend.Observation;

public class TestObservationFilter {
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

	private void checkObservation(Observation obs) {
		// observation -> URL -> meta is just a fast way of getting an observationMeta
		String obsURL = URLScheme.observationURL(obs);
		ObservationMeta meta = URLScheme.parseObservationURL(obsURL);
		Observation shouldMatch = ObservationFilter.retrieveFromMeta(meta, ds);
		assertNotNull(shouldMatch);
		assertTrue(obs.equals(shouldMatch));
		
		// Try retrieving some junk, just because we can
		meta.cell = meta.cell + "AKSJD";
		assertNull(null, ObservationFilter.retrieveFromMeta(meta, ds));
	}

	@Test
	public void testGetFromMeta() {
		Cell cell = ds.dggsCell("R7852");
		checkObservation(cell.pixelObservation(4));
		checkObservation(cell.tileObservation(3));
	}
}
