package anuled.dynamicstore.rdfmapper;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import anuled.dynamicstore.TestData;
import anuled.dynamicstore.backend.Cell;
import anuled.dynamicstore.backend.HDF5Dataset;
import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.rdfmapper.properties.ObservationProperty;
import anuled.dynamicstore.rdfmapper.properties.PropertyIndex;
import anuled.vocabulary.LED;
import anuled.vocabulary.QB;

public class TestObservationFilter {
	private HDF5Dataset ds;
	private static TestData td;
	private ObservationFilter filter;

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
		filter = new ObservationFilter(ds);
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
	
	private void checkTypeFilterCount(int expected, Resource val) {
		filter = new ObservationFilter(ds);
		ObservationProperty typeFilter = PropertyIndex.getProperty(RDF.type.getURI());
		typeFilter.applyToFilter(filter, val.asNode());
		assertEquals(expected, filter.execute().count());
	}
	
	@Test
	public void testFilterByType() {
		// First, count the number of observations (no filtering)
		assertEquals(84, filter.execute().count());
		checkTypeFilterCount(0, LED.time);
		checkTypeFilterCount(84, QB.Observation);
		checkTypeFilterCount(42, LED.Pixel);
		checkTypeFilterCount(42, LED.GridSquare);
	}
}
