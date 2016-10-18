package anuled.dynamicstore.util;

import static anuled.dynamicstore.Util.*;
import static org.junit.Assert.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.XSD;
import org.junit.Test;

import anuled.dynamicstore.Util;

public class TestUtil {
	@Test
	public void testConstructor() {
		// does nothing, thanks JaCoCo
		new Util();
	}

	@Test
	public void testToDouble() {
		// make sure that conversion to number works flawlessly
		Node[] nodes = { createLiteralNode((int) 42),
				createLiteralNode((float) -13.2),
				createLiteralNode((long) 21932),
				createLiteralNode((double) 15.3), createLiteralNode((int) 0),
				createLiteralNode("14.5", XSD.xdouble.getURI()),
				createLiteralNode("14.3", XSD.xfloat.getURI()) };
		double[] trueValues = { 42.0, -13.2, 21932.0, 15.3, 0.0, 14.5, 14.3 };
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

	@Test
	public void testToInt() {
		Node[] nodes = { createLiteralNode((int) 42),
				createLiteralNode((double) -3),
				createLiteralNode((float) 17),
				createLiteralNode("13", XSD.integer.getURI()),
				createLiteralNode("-9", XSD.xint.getURI()) };
		Integer[] trueValues = { 42, -3, 17, 13, -9 };
		assertEquals(nodes.length, trueValues.length);
		for (int i = 0; i < nodes.length; i++) {
			Optional<Integer> value = toInt(nodes[i]);
			assertTrue(value.isPresent());
			assertEquals(trueValues[i], value.get());
		}

		assertFalse(toInt(createLiteralNode("A string")).isPresent());
		assertFalse(toInt(createLiteralNode(13.5)).isPresent());
		assertFalse(toInt(createURINode("http://fake/")).isPresent());
	}

	@Test
	public void testCanonicalTime() {
		ZonedDateTime someTime = ZonedDateTime.of(2016, 5, 3, 4, 2, 13, 9,
				ZoneOffset.ofHours(10));
		// Make sure it converts it to UTC in the way we want, including Z
		// specifier and dropping of anything below second precision.
		String expected = "2016-05-02T18:02:13Z";
		assertEquals(expected, canonicalTimeString(someTime));
	}
}
