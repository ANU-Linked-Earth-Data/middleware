package anuled.dynamicstore.rdfmapper.properties;

import static org.junit.Assert.*;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import anuled.dynamicstore.TestData;
import anuled.dynamicstore.Util;
import anuled.dynamicstore.backend.Cell;
import anuled.dynamicstore.backend.HDF5Dataset;
import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.backend.PixelObservation;
import anuled.dynamicstore.backend.Product;
import anuled.dynamicstore.backend.TileObservation;
import anuled.vocabulary.Geo;
import anuled.vocabulary.LED;
import anuled.vocabulary.QB;

public class TestGetProperties {
	private HDF5Dataset ds;
	private static TestData td;
	private Cell cell;
	private PixelObservation pxObs;
	private TileObservation tlObs;

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
		cell = ds.dggsCell("R7852");
		ZonedDateTime timestamp = ZonedDateTime.parse("2013-05-27T23:58:20Z");
		Product product = new Product(ds, "LS8_OLI_TIRS_NBAR");
		pxObs = cell.pixelObservation(product, timestamp, 4);
		tlObs = cell.tileObservation(product, timestamp, 5);
	}

	@After
	public void tearDown() {
		ds.dispose();
	}

	private List<Node> getProp(Resource prop, Observation obs) {
		return getProp(prop.getURI(), obs);
	}

	private List<Node> getProp(String prop, Observation obs) {
		ObservationProperty fetcher = PropertyIndex.getProperty(prop).get();
		return fetcher.valuesForObservation(obs, "http://fake/")
				.collect(Collectors.toList());
	}

	@Test
	public void testType() {
		List<Node> pxProps = getProp(RDF.type, pxObs);
		List<Node> tlProps = getProp(RDF.type, tlObs);
		// Common sanity checks (number of types, must have Observation type)
		Stream.of(pxProps, tlProps).forEach(p -> {
			assertEquals(2, p.size());
			assertTrue(p.contains(QB.Observation.asNode()));
		});
		// Check that additional custom types are correct
		assertTrue(pxProps.contains(LED.Pixel.asNode()));
		assertTrue(tlProps.contains(LED.GridSquare.asNode()));
	}

	private void assertNodeListIsEqual(double expected, List<Node> nodeList,
			double delta) {
		assertEquals(1, nodeList.size());
		Node fst = nodeList.get(0);
		assertTrue(fst.isLiteral());
		float actual = Float.parseFloat(fst.getLiteralLexicalForm());
		assertEquals(expected, actual, delta);
	}

	private void assertNodeListIsURI(String uri, List<Node> nodeList) {
		Node nodeURI = Util.createURINode(uri);
		assertNodeListIs(nodeURI, nodeList);
	}

	private void assertNodeListIsLiteral(Object val, List<Node> nodeList) {
		Node nodeVal = Util.createLiteralNode(val);
		assertNodeListIs(nodeVal, nodeList);
	}

	private void assertNodeListIs(Node expectedValue, List<Node> nodeList) {
		assertEquals(1, nodeList.size());
		assertTrue(expectedValue.equals(nodeList.get(0)));
	}

	@Test
	public void testBand() {
		assertNodeListIsLiteral(5, getProp(LED.etmBand, tlObs));
	}

	@Test
	public void testDGGSInfo() {
		assertNodeListIsLiteral("R7852", getProp(LED.dggsCell, pxObs));
		assertNodeListIsLiteral("R7852", getProp(LED.dggsCell, tlObs));

		assertNodeListIsLiteral(5, getProp(LED.dggsLevelSquare, pxObs));
		assertNodeListIsLiteral(5, getProp(LED.dggsLevelSquare, tlObs));

		assertNodeListIsLiteral(5, getProp(LED.dggsLevelPixel, pxObs));
		assertNodeListIsLiteral(7, getProp(LED.dggsLevelPixel, tlObs));
	}

	@Test
	public void testDataset() {
		assertNodeListIsURI("http://fake/", getProp(QB.dataSet, pxObs));
	}

	@Test
	public void testLocation() {
		// bounds, lat, long
		assertNodeListIsEqual(149.44, getProp(Geo.long_, tlObs), 1e-2);
		assertNodeListIsEqual(-35.24, getProp(Geo.lat, tlObs), 1e-2);

		List<Node> boundsNodes = getProp(LED.bounds, pxObs);
		assertEquals(1, boundsNodes.size());
		Node fst = boundsNodes.get(0);
		assertTrue(fst.isLiteral());
		String boundsWKT = fst.getLiteralLexicalForm();

		// Extract the coordinate list (lon lat points)
		String prefix = "POLYGON((";
		String suffix = "))";
		assertTrue(boundsWKT.startsWith(prefix));
		assertTrue(boundsWKT.endsWith(suffix));
		String innnerList = boundsWKT.substring(prefix.length(),
				boundsWKT.length() - suffix.length());

		// Now this is going to get really messy
		double[][] truePolyPoints = { { 148.889, -34.6638 }, { 150, -34.6638 },
				{ 150, -35.8186 }, { 148.889, -35.8186 },
				{ 148.889, -34.6638 } };
		int pointIdx = 0;
		for (String pair : innnerList.split(",")) {
			assertTrue(pointIdx < truePolyPoints.length);
			String[] coordStr = pair.trim().split("\\s+", 2);
			assertEquals(2, coordStr.length);
			float actualLon = Float.parseFloat(coordStr[0].trim());
			float actualLat = Float.parseFloat(coordStr[1].trim());
			assertEquals(actualLon, truePolyPoints[pointIdx][0], 1e-3);
			assertEquals(actualLat, truePolyPoints[pointIdx][1], 1e-3);
			pointIdx++;
		}
		assertEquals(pointIdx, truePolyPoints.length);
	}

	@Test
	public void testData() {
		// Nonsensical properties
		assertEquals(0, getProp(LED.value, tlObs).size());
		assertEquals(0, getProp(LED.imageData, pxObs).size());

		assertNodeListIsEqual(5192.7001, getProp(LED.value, pxObs), 1e-3);
		List<Node> tlData = getProp(LED.imageData, tlObs);
		assertEquals(1, tlData.size());
		Node real = tlData.get(0);
		assertTrue(real.isURI());
		// Just assume that the URI is okay. We can test the underlying
		// functions elsewhere.
	}

	@Test
	public void testResolution() {
		assertNodeListIsEqual(0.8826, getProp(LED.resolution, pxObs), 1e-3);
		assertNodeListIsEqual(7.9438, getProp(LED.resolution, tlObs), 1e-3);
	}

	@Test
	public void testTime() {
		assertNodeListIsLiteral(
				GregorianCalendar.from(pxObs.getTimestamp()),
				getProp(LED.time, pxObs));
	}

	@Test
	public void testMBR() {
		// test minimum bounding rectangle properties
		assertNodeListIsEqual(-35.8186, getProp(LED.latMin, tlObs), 1e-1);
		assertNodeListIsEqual(-34.6638, getProp(LED.latMax, tlObs), 1e-1);
		assertNodeListIsEqual(148.889, getProp(LED.longMin, tlObs), 1e-2);
		assertNodeListIsEqual(150.0, getProp(LED.longMax, tlObs), 1e-2);
	}

	@Test
	public void testLLBP() {
		// LatLonBoxProperty should error when you get its value
		try {
			getProp(LatLonBoxProperty.BoundType.BoxBottom.getURI(), tlObs);
		} catch (RuntimeException e) {
			assertTrue(e.getMessage()
					.contains("Property is internal, and not intended"));
			return;
		}
		fail("Should error out before getting here");
	}
}
