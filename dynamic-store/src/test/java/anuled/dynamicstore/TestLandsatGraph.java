package anuled.dynamicstore;

import java.io.IOException;

import org.apache.jena.graph.Triple;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestLandsatGraph {
	private LandsatGraph graph;
	private static TestData td;
	
	@BeforeClass
	public static void setUpClass() throws IOException {
		td = new TestData();
	}
	
	@AfterClass
	public static void tearDownClass() {
		td.close();
	}

	@Before
	public void setUp() throws Exception {
		graph = new LandsatGraph(td.getPath());
	}

	@Test
	public void testGraphBaseFindTriple() {
		Triple pattern = Triple.createMatch(null, null, null);
		// Just try getting back the iterator (don't bother with anything else
		// in this test, because we can use a higher-level API to test actual
		// graph contents)
		graph.graphBaseFind(pattern);
	}
}
