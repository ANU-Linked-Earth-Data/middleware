package anuled.dynamicstore;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.RDF;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import anuled.vocabulary.QB;

public class TestHighLevelDataset {
	static TestData td;
	static String prefix = "https://hld-test.example.com/data/testgraph/";
	HighLevelDataset hld;

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
		hld = new HighLevelDataset(td.getPath(), prefix);
	}
	
	@Test
	public void testPrefix() {
		assertEquals(prefix, hld.getPrefix());
	}

	@Test
	public void testUnionComponents() {
		Graph everything = hld.getQBGraph();

		// find the qb:Dataset
		List<Triple> trips = everything
				.find(null, RDF.type.asNode(), QB.DataSet.asNode()).toList();
		assertEquals(1, trips.size());
		Triple firstTrip = trips.get(0);
		assertEquals(RDF.type.asNode(), firstTrip.getPredicate());
		assertEquals(QB.DataSet.asNode(), firstTrip.getObject());
		Node datasetNode = firstTrip.getSubject();
		assertTrue(datasetNode.getURI().startsWith(prefix));

		// now find a single qb:Observation
		trips = everything
				.find(null, RDF.type.asNode(), QB.Observation.asNode())
				.toList();
		assertEquals(84, trips.size());
		for (Triple trip : trips) {
			assertEquals(RDF.type.asNode(), trip.getPredicate());
			assertEquals(QB.Observation.asNode(), trip.getObject());
			assertTrue(firstTrip.getSubject().getURI().startsWith(prefix));
		}

		// does the observation match the dataset?
		Node someObs = trips.get(0).getPredicate();
		trips = everything.find(someObs, QB.DataSet.asNode(), null).toList();
		assertEquals(1, trips.size());
		assertEquals(datasetNode, trips.get(0));
	}

}
