package anuled.dynamicstore.sparqlopt;

import static org.junit.Assert.*;
import static org.apache.jena.graph.NodeFactory.*;

import java.util.Arrays;

import org.apache.jena.graph.Triple;
import org.junit.Test;

// I'm genuinely proud of the noun pile that is the name of this class.
public class ObservationGraphStageGeneratorTest {

	@Test
	public void testTripleSorting() {
		// Test the triple comparator. Instead of testing at a low level, we'll
		// just use it to sort something (isn't that what we want anyway?).
		Triple[] expected = {
				new Triple(createBlankNode(), createBlankNode(),
						createBlankNode()),
				new Triple(createBlankNode(), createBlankNode(),
						createVariable("aardvark")),
				new Triple(createBlankNode(), createBlankNode(),
						createVariable("bar")),
				new Triple(createVariable("bar"),
						createVariable("foo"),
						createURI("http://example.com")),
				new Triple(createVariable("foo"), createVariable("bar"),
						createVariable("baz")) };
		Triple[] unordered = { expected[2], expected[1], expected[4],
				expected[0], expected[3] };
		Arrays.sort(unordered,
				new ObservationGraphStageGenerator.TripleComparator());
		assertEquals(expected.length, unordered.length);
		for (int i = 0; i < unordered.length; i++) {
			assertEquals(expected[i], unordered[i]);
		}
	}

}
