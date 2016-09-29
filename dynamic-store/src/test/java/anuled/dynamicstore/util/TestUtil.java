package anuled.dynamicstore.util;

import static anuled.dynamicstore.util.JenaUtil.*;
import static org.junit.Assert.*;

import java.util.Optional;

import org.apache.jena.graph.Node;
import org.junit.Test;

public class TestUtil {
	@Test
	public void testConstructor() {
		// does nothing, thanks JaCoCo
		new JenaUtil();
	}

	@Test
	public void testToNumber() {
		// make sure that conversion to number works flawlessly
		Node[] nodes = { createLiteralNode((int) 42),
				createLiteralNode((float) -13.2),
				createLiteralNode((long) 21932),
				createLiteralNode((double) 15.3), createLiteralNode((int) 0) };
		double[] trueValues = { 42.0, -13.2, 21932.0, 15.3, 0.0 };
		assertEquals(nodes.length, trueValues.length);
		for (int i = 0; i < nodes.length; i++) {
			Optional<Double> value = toDouble(nodes[i]);
			assertTrue(value.isPresent());
			assertEquals(trueValues[i], value.get(), 1e-7);
		}
		
		// now test error cases
		assertFalse(toDouble(createLiteralNode("A string")).isPresent());
		assertFalse(toDouble(createURINode("http://fake/")).isPresent());
	}
}
