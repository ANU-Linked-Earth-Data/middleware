package anuled.dynamicstore;

import org.apache.jena.graph.Triple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestLandsatGraph {
	private LandsatGraph graph;
	private TestGeotiff testGT;

	@Before
	public void setUp() throws Exception {
		testGT = new TestGeotiff();
		graph = new LandsatGraph(testGT.getPath());
	}

	@After
	public void tearDown() {
		testGT.dispose();
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
