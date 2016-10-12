package anuled.dynamicstore;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import anuled.dynamicstore.backend.Observation;
import anuled.vocabulary.LED;
import anuled.vocabulary.QB;

public class TestObservationGraph {
	private ObservationGraph graph;
	private Model model;
	private static TestData td;
	private static String prefixes = "prefix ogc: <http://www.opengis.net/ont/geosparql#>\n"
			+ "prefix qb: <http://purl.org/linked-data/cube#>\n"
			+ "prefix led: <http://www.anulinkedearth.org/sandbox/ANU-LED#>\n"
			+ "prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n"
			+ "prefix owl: <http://www.w3.org/2002/07/owl#>\n"
			+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "prefix xml: <http://www.w3.org/XML/1998/namespace>\n"
			+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n"
			+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";
	private static String awesomeURI = "https://anulinkedearth.org/rdf/"
			+ "observation/LS8_OLI_TIRS_NBAR/2013/05/27/23/58/20/cell/R78/levelSquare-3/"
			+ "levelPixel-5/band-5";

	private ResultSet runSelect(String query) {
		Query q = QueryFactory.create(prefixes + "\n" + query);
		return QueryExecutionFactory.create(q, model).execSelect();
	}

	@BeforeClass
	public static void setUpClass() throws IOException {
		td = new TestData();
	}

	@AfterClass
	public static void tearDownClass() {
		td.dispose();
	}

	@Before
	public void setUp() throws Exception {
		graph = new ObservationGraph(td.getPath(), "http://example.com/fakeDS");
		model = new ModelCom(graph);
	}

	@Test
	public void testGetFromURI() {
		Observation obs = graph.obsForURI(awesomeURI);
		assertEquals("R78", obs.getCell().getDGGSIdent());
		assertEquals(3, obs.getCellLevel());
		assertEquals(5, obs.getPixelLevel());
		assertEquals(5, obs.getBand());

		assertNull(graph.obsForURI(
				"https://anulinkedearth.org/rdf/observation/aswafasfas"));
		assertNull(graph.obsForURI("something really broken"));
	}

	@Test
	public void testMatchingObservations() {
		Node awesomeNode = Util.createURINode(awesomeURI);
		Stream<Observation> allObs = graph.matchingObservations(awesomeNode,
				null, null);
		assertEquals(1, allObs.count());

		Node awfulNode = Util.createURINode("https://this/is/garbage");
		allObs = graph.matchingObservations(awfulNode, null, null);
		assertEquals(0, allObs.count());

		allObs = graph.matchingObservations(null, LED.imageData.asNode(), null);
		assertEquals(84, allObs.count()); // should return everything

		allObs = graph.matchingObservations(null,
				Util.createLiteralNode("Oh dear"), null);
		assertEquals(0, allObs.count()); // literal predicates? Nope.

		allObs = graph.matchingObservations(null, RDFS.range.asNode(), null);
		assertEquals(0, allObs.count()); // nonexistent predicate

		allObs = graph.matchingObservations(
				Util.createLiteralNode("something"), null, null);
		assertEquals(0, allObs.count()); // literal subjects
	}

	@Test
	public void testObservationURIs() {
		List<Triple> trips = Arrays.asList();
		// should give us everything
		assertEquals(84, graph.observationURIs(trips).count());
		Var missingVar = Var.alloc("s");
		trips = Arrays.asList(
				new Triple(missingVar, RDF.type.asNode(),
						LED.GridSquare.asNode()),
				new Triple(missingVar, LED.etmBand.asNode(),
						Util.createLiteralNode(2)));
		assertEquals(6, graph.observationURIs(trips).count());
	}

	@Test
	public void testMapToTriples() {
		Observation obs = graph.obsForURI(awesomeURI);
		// non-URI predicate should yield no triples
		assertEquals(0,
				graph.mapToTriples(obs,
						Util.createLiteralNode("some literal"), null)
						.count());
		// query for a URI we no know nothing about should yield no triples
		assertEquals(
				0, graph
						.mapToTriples(obs,
								Util.createURINode(
										"http://example.com/dontexist"),
								null)
						.count());

		List<Triple> trips = graph
				.mapToTriples(obs, null, QB.Observation.asNode())
				.collect(Collectors.toList());
		assertEquals(1, trips.size());
		Triple firstTrip = trips.get(0);
		assertEquals(awesomeURI, firstTrip.getSubject().getURI());
		assertEquals(RDF.type.asNode(), firstTrip.getPredicate());
		assertEquals(QB.Observation.asNode(), firstTrip.getObject());

		// now make sure we get two triples back for general types
		// (qb:Observation and LED:GridSquare or LED:Pixel)
		assertEquals(2,
				graph.mapToTriples(obs, RDF.type.asNode(), null).count());
	}

	@Test
	public void testGraphBaseFindTriple() {
		Triple pattern = Triple.createMatch(null, null, null);
		// Just try getting back the iterator (don't bother with anything else
		// in this test, because we can use a higher-level API to test actual
		// graph contents)
		ExtendedIterator<Triple> trips = graph.graphBaseFind(pattern);
		assertTrue(trips.toList().size() > 100);
	}

	@Test
	public void testQuery() {
		ResultSet results = runSelect("SELECT * WHERE {?s ?p ?o.} LIMIT 10");
		// Make sure a query for everything //actually executes//.
		assertTrue(results.hasNext());
		// Now make sure that all qb:observations are actually qb:observations
		results = runSelect("SELECT ?s WHERE {?s a qb:Observation .}");
		assertTrue(results.hasNext());
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			String uri = qs.getResource("s").getURI();
			assertTrue(uri
					.startsWith("https://anulinkedearth.org/rdf/observation/"));
		}
	}

	@Test
	public void testFilteredQuery() {
		ResultSet results = runSelect("SELECT DISTINCT ?cell WHERE {"
				+ "?s a qb:Observation; led:latMin ?minLat"
				+ "; led:latMax ?maxLat; led:longMin ?minLong"
				+ "; led:longMax ?maxLong; led:dggsCell ?cell."
				+ "FILTER(?minLat > -35.45 && -35 > ?maxLat &&"
				+ "148.8 <= ?minLong && ?maxLong <= 149.3)}");
		Set<String> allCells = new HashSet<>();
		results.forEachRemaining(sol -> {
			String cellID = sol.getLiteral("cell").getString();
			allCells.add(cellID);
		});
		Set<String> expectedCells = new HashSet<>(Arrays.asList("R78523"));
		assertEquals(expectedCells, allCells);
	}
}
