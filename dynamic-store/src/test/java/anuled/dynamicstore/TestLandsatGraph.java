package anuled.dynamicstore;

import org.apache.jena.graph.Triple;
import org.junit.Before;
import org.junit.Test;

public class TestLandsatGraph {
	private LandsatGraph graph;

	@Before
	public void setUp() throws Exception {
		graph = new LandsatGraph(TestData.TEST_H5_NAME);
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
