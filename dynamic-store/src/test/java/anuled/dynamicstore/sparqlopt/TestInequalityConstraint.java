package anuled.dynamicstore.sparqlopt;

import static anuled.dynamicstore.Util.*;
import static anuled.dynamicstore.sparqlopt.InequalityConstraint.*;
import static org.apache.jena.sparql.sse.SSE.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;

public class TestInequalityConstraint {
	Node uriNode = createURINode("http://fake/");
	Node blankNode = NodeFactory.createBlankNode();
	Node numberNode = createLiteralNode(42);
	Node varNode = Var.alloc("s");

	// these should execute fine
	InequalityConstraint vvConstraint = new InequalityConstraint(varNode,
			varNode, ConstraintType.LESS),
			lvConstraint = new InequalityConstraint(numberNode, varNode,
					ConstraintType.LESS_EQ),
			llConstraint = new InequalityConstraint(numberNode, numberNode,
					ConstraintType.LESS_EQ);

	@Test
	public void testFromExpr() {
		assertFalse(fromExpr(parseExpr("(regex ?str \"foobar\")")).isPresent());
		assertFalse(fromExpr(parseExpr("(eq ?s 42)")).isPresent());

		// Parsed values
		InequalityConstraint leqConstraint = fromExpr(parseExpr("(<= ?s 42)"))
				.get(), ltConstraint = fromExpr(parseExpr("(< ?s 42)")).get(),
				gtConstraint = fromExpr(parseExpr("(> ?s 42)")).get(),
				geqConstraint = fromExpr(parseExpr("(>= ?s 42)")).get();
		// Expected values
		InequalityConstraint leqExpected = new InequalityConstraint(varNode,
				numberNode, ConstraintType.LESS_EQ),
				ltExpected = new InequalityConstraint(varNode, numberNode,
						ConstraintType.LESS),
				geqExpected = new InequalityConstraint(numberNode, varNode,
						ConstraintType.LESS_EQ),
				gtExpected = new InequalityConstraint(numberNode, varNode,
						ConstraintType.LESS);
		assertTrue(leqExpected.sameAs(leqConstraint));
		assertTrue(ltExpected.sameAs(ltConstraint));
		assertTrue(geqExpected.sameAs(geqConstraint));
		assertTrue(gtExpected.sameAs(gtConstraint));
	}

	@Test
	public void testToString() {
		assertEquals("?s < ?s", vvConstraint.toString());
		assertEquals("\"42\"^^http://www.w3.org/2001/XMLSchema#int <= ?s",
				lvConstraint.toString());
	}

	@Test
	public void testHashEquals() {
		for (InequalityConstraint constraint : Arrays.asList(vvConstraint,
				lvConstraint, llConstraint)) {
			assertTrue(constraint.hashCode() == constraint.hashCode()
					&& constraint.equals(constraint));
		}
		assertNotEquals(vvConstraint, lvConstraint);
		assertEquals(new InequalityConstraint(createLiteralNode(42),
				Var.alloc("s"), ConstraintType.LESS_EQ), lvConstraint);
		assertNotEquals(new InequalityConstraint(numberNode, numberNode,
				ConstraintType.LESS), llConstraint);
	}

	@Test
	public void testConstruct() {
		// these should all fail
		try {
			new InequalityConstraint(numberNode, blankNode,
					ConstraintType.LESS);
			assertTrue("Construction should have failed", false);
		} catch (AssertionError e) {
		}

		try {
			new InequalityConstraint(uriNode, varNode, ConstraintType.LESS);
			assertTrue("Construction should have failed", false);
		} catch (AssertionError e) {
		}

		try {
			new InequalityConstraint(uriNode, uriNode, ConstraintType.LESS_EQ);
			assertTrue("Construction should have failed", false);
		} catch (AssertionError e) {
		}
	}

}
