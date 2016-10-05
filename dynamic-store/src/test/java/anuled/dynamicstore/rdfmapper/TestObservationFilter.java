package anuled.dynamicstore.rdfmapper;

import static anuled.dynamicstore.rdfmapper.properties.LatLonBoxProperty.BoundType.*;
import static anuled.dynamicstore.util.JenaUtil.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
import anuled.dynamicstore.backend.PixelObservation;
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

	private void checkObservation(Observation obs)
			throws CloneNotSupportedException {
		// observation -> URL -> meta is just a fast way of getting an
		// observationMeta
		String obsURL = URLScheme.observationURL(obs);
		ObservationMeta meta = URLScheme.parseObservationURL(obsURL);
		Observation shouldMatch = ObservationFilter.retrieveFromMeta(meta, ds);
		assertNotNull(shouldMatch);
		assertTrue(obs.equals(shouldMatch));

		// Try retrieving some junk, just because we can
		ObservationMeta brokenCellMeta = meta.clone();
		brokenCellMeta.cell = brokenCellMeta.cell + "AKSJD";
		assertNull(ObservationFilter.retrieveFromMeta(brokenCellMeta, ds));

		ObservationMeta brokenLevelMeta = meta.clone();
		brokenLevelMeta.levelPixel = -1;
		assertNull(ObservationFilter.retrieveFromMeta(brokenLevelMeta, ds));
	}

	@Test
	public void testGetFromMeta() throws CloneNotSupportedException {
		Cell cell = ds.dggsCell("R7852");
		checkObservation(cell.pixelObservation(4));
		checkObservation(cell.tileObservation(3));
	}

	private void checkTypeFilterCount(int expected, Resource val) {
		filter = new ObservationFilter(ds);
		ObservationProperty typeFilter = PropertyIndex
				.getProperty(RDF.type.getURI()).get();
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

	@Test
	public void testIncompatibleType() {
		filter.constrainToPixel().constrainToPixel();
		assertEquals(84 / 2, filter.execute().count());
		filter.constrainToTile();
		assertEquals(0, filter.execute().count());
	}

	@Test
	public void testIncompatibleLevel() {
		filter.constrainLevel(3).constrainLevel(3);
		assertEquals(14, filter.execute().count());
		filter.constrainLevel(5);
		assertEquals(0, filter.execute().count());
	}

	@Test
	public void testIncompatibleBand() {
		// double-specification of bands is fine iff it's the same band
		filter.constrainBandNum(3).constrainBandNum(3);
		assertEquals(84 / 7, filter.execute().count());
		filter.constrainBandNum(4);
		assertEquals(0, filter.execute().count());
	}

	@Test
	public void testIncompatibleCellID() {
		filter.constrainCellID("R78").constrainCellID("R78")
				.constrainCellID("R91");
		assertEquals(0, filter.execute().count());
	}

	@Test
	public void testConstrainLocation() {
		filter.constrainLatMin(-35.45).constrainLatMax(-35)
				.constrainLonMin(148.8).constrainLonMax(149.3)
				.constrainProperty(RDF.type.getURI(), LED.GridSquare.asNode())
				.constrainProperty(LED.etmBand.getURI(), createLiteralNode(0));
		List<String> cellIDs = filter.execute()
				.map(obs -> obs.getCell().getDGGSIdent())
				.collect(Collectors.toList());
		assertEquals(1, cellIDs.size());
		assertEquals("R78523", cellIDs.get(0));
	}

	@Test
	public void testNonexistentProperty() {
		filter.constrainProperty("http://example.com/doesntExist",
				createLiteralNode(42));
		assertEquals(0, filter.execute().count());
	}

	@Test
	public void testConstrainNaively() {
		// there are some thing that we can't really constrain by in an efficent
		// way
		filter.constrainNaively(PropertyIndex.getProperty(LED.etmBand).get(),
				createLiteralNode(3));
		List<Observation> allObs = filter.execute()
				.collect(Collectors.toList());
		// there are 84 observations total in the test set, and 7 bands, so we
		// should get back 1/7th of the observations
		assertEquals(84 / 7, allObs.size());
		for (Observation obs : allObs) {
			assertEquals(3, obs.getBand());
		}
	}

	@Test
	public void testIndirectNaiveConstraint() {
		// we can only really filter values naively, since we don't have an
		// inverted index for them
		filter.constrainProperty(LED.value.getURI(),
				createLiteralNode(new Double(931.0)));
		List<Observation> allObs = filter.execute()
				.collect(Collectors.toList());
		assertEquals(1, allObs.size());
		for (Observation obs : allObs) {
			assertTrue(obs instanceof PixelObservation);
			assertEquals(931.0, ((PixelObservation) obs).getPixel(), 0.001);
		}
	}

	@Test
	public void testFilterByDGGSSquare() {
		filter.constrainProperty(LED.dggsCell.getURI(),
				createLiteralNode("R7852"));
		List<Observation> allObs = filter.execute()
				.collect(Collectors.toList());
		assertEquals(14, allObs.size());
		for (Observation obs : allObs) {
			assertEquals("R7852", obs.getCell().getDGGSIdent());
		}

		// compatible constrain doesn't change anything
		filter.constrainProperty(LED.dggsCell.getURI(),
				createLiteralNode("R7852"));
		assertEquals(14, filter.execute().count());

		// incompatible constraint breaks everything
		filter.constrainProperty(LED.dggsCell.getURI(),
				createLiteralNode("R785"));
		assertEquals(0, filter.execute().count());

		filter.constrainProperty(LED.dggsCell.getURI(),
				createURINode("http://not-a-literal/"));
		assertEquals(0, filter.execute().count());
	}

	@Test
	public void testFilterByNonExistentSquare() {
		filter.constrainProperty(LED.dggsCell.getURI(),
				createLiteralNode("R8192"));
		assertEquals(0, filter.execute().count());
	}

	@Test
	public void testFilterByBand() {
		filter.constrainProperty(LED.etmBand.getURI(), createLiteralNode(3));
		List<Observation> allObs = filter.execute()
				.collect(Collectors.toList());
		assertEquals(12, allObs.size());
		for (Observation obs : allObs) {
			assertEquals(3, obs.getBand());
		}

		filter.constrainProperty(LED.etmBand.getURI(), createLiteralNode(3));
		assertEquals(12, filter.execute().count());

		filter.constrainProperty(LED.etmBand.getURI(), createLiteralNode(4));
		assertEquals(0, filter.execute().count());

		filter.constrainProperty(LED.etmBand.getURI(),
				createURINode("http://not-a-literal/"));
		assertEquals(0, filter.execute().count());
	}

	@Test
	public void testFilterByNonexistentBand() {
		filter.constrainProperty(LED.etmBand.getURI(), createLiteralNode(17));
		assertEquals(0, filter.execute().count());
	}

	@Test
	public void testFilterByLevel() {
		filter.constrainProperty(LED.dggsLevelSquare.getURI(),
				createLiteralNode(5));
		List<Observation> allObs = filter.execute()
				.collect(Collectors.toList());
		assertEquals(14, allObs.size());
		for (Observation obs : allObs) {
			assertEquals(5, obs.getCellLevel());
		}

		filter.constrainProperty(LED.dggsLevelSquare.getURI(),
				createLiteralNode(5));
		assertEquals(14, filter.execute().count());

		filter.constrainProperty(LED.dggsLevelSquare.getURI(),
				createLiteralNode(6));
		assertEquals(0, filter.execute().count());

		filter.constrainProperty(LED.dggsLevelSquare.getURI(),
				createURINode("http://not-a-literal/"));
		assertEquals(0, filter.execute().count());
	}

	@Test
	public void testFilterByNonexistentLevel() {
		filter.constrainProperty(LED.dggsLevelSquare.getURI(),
				createLiteralNode(21));
		assertEquals(0, filter.execute().count());
	}

	@Test
	public void testComposeFilters() {
		filter.constrainProperty(LED.dggsCell.getURI(),
				createLiteralNode("R7852"))
				.constrainProperty(LED.etmBand.getURI(), createLiteralNode(5))
				.constrainProperty(LED.dggsLevelSquare.getURI(),
						createLiteralNode(5))
				.constrainProperty(RDF.type.getURI(), LED.Pixel.asNode());
		List<Observation> allObs = filter.execute()
				.collect(Collectors.toList());
		assertEquals(1, allObs.size());
		Observation firstObs = allObs.get(0);
		assertEquals(5, firstObs.getBand());
		assertEquals("R7852", firstObs.getCell().getDGGSIdent());
		assertEquals(5, firstObs.getCellLevel());
	}

	@Test
	public void testIrrelevantLatLonBoxProperty() {
		// Shouldn't do anything
		filter.constrainProperty(BoxBottom.getURI(), createLiteralNode(-80.0));
		assertEquals(84, filter.execute().count());
	}

	@Test
	public void testInvalidLatLonBoxProperty() {
		filter.constrainProperty(BoxRight.getURI(),
				createLiteralNode("clearly not a number"));
		assertEquals(0, filter.execute().count());
	}

	@Test
	public void testMeaningfulLatLonBoxProperty() {
		filter.constrainProperty(BoxBottom.getURI(), createLiteralNode(-35.45))
				.constrainProperty(BoxTop.getURI(), createLiteralNode(-35))
				.constrainProperty(BoxLeft.getURI(), createLiteralNode(148.8))
				.constrainProperty(BoxRight.getURI(), createLiteralNode(149.3))
				.constrainProperty(RDF.type.getURI(), LED.GridSquare.asNode())
				.constrainProperty(LED.etmBand.getURI(), createLiteralNode(0));
		List<Observation> out = filter.execute().collect(Collectors.toList());
		assertEquals(1, out.size());
		assertEquals("R78523", out.get(0).getCell().getDGGSIdent());

	}
}
