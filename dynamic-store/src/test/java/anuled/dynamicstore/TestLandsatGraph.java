package anuled.dynamicstore;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestLandsatGraph {
	private LandsatGraph graph;
	private Model model;
	private static TestData td;
	private static String prefixes = "prefix ogc: <http://www.opengis.net/ont/geosparql#>\n"
			+ "prefix qb: <http://purl.org/linked-data/cube#>\n"
			+ "prefix led: <http://www.example.org/ANU-LED#>\n"
			+ "prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n"
			+ "prefix owl: <http://www.w3.org/2002/07/owl#>\n"
			+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "prefix xml: <http://www.w3.org/XML/1998/namespace>\n"
			+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n"
			+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";

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
		graph = new LandsatGraph(td.getPath());
		model = new ModelCom(graph);
	}

	@Test(timeout=30000)
	public void testGraphBaseFindTriple() {
		Triple pattern = Triple.createMatch(null, null, null);
		// Just try getting back the iterator (don't bother with anything else
		// in this test, because we can use a higher-level API to test actual
		// graph contents)
		ExtendedIterator<Triple> trips = graph.graphBaseFind(pattern);
		assertTrue(trips.toList().size() > 100);
	}

	@Test(timeout=30000)
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
			assertTrue(uri.startsWith("https://anulinkedearth.org/rdf/observation/"));
		}
	}
}
