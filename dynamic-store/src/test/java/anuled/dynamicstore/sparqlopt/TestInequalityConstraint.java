package anuled.dynamicstore.sparqlopt;

import static org.junit.Assert.*;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;

import static anuled.dynamicstore.util.JenaUtil.*;

import org.junit.Test;

import anuled.dynamicstore.sparqlopt.InequalityConstraint.ConstraintType;

public class TestInequalityConstraint {

	@Test
	public void testFromExpr() {
		// TODO: Test the fromExpr method
	}

	@Test
	public void testConstruct() {
		Node uriNode = createURINode("http://fake/");
		Node blankNode = NodeFactory.createBlankNode();
		Node numberNode = createLiteralNode(42);
		Node varNode = Var.alloc("s");

		// these should execute fine
		new InequalityConstraint(varNode, varNode, ConstraintType.LESS);
		new InequalityConstraint(numberNode, varNode, ConstraintType.LESS_EQ);
		new InequalityConstraint(numberNode, numberNode,
				ConstraintType.LESS_EQ);

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
