package anuled.dynamicstore.sparqlopt;

import static anuled.dynamicstore.Util.*;
import static org.junit.Assert.*;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

public class TestFilteredBasicPattern {

	@Test
	public void testFilteredBasicPattern() {
		// make sure it's wrapping BasicPattern correctly
		BasicPattern bp = new BasicPattern();
		bp.add(new Triple(createURINode("http://fake/"),
				createURINode("http://fake/"),
				createLiteralNode("some", "http://thing/")));
		FilteredBasicPattern fbp = new FilteredBasicPattern(bp);
		assertEquals(bp.getList(), fbp.getList());
	}

	@Test
	public void testAddExprList() {
		FilteredBasicPattern fbp = new FilteredBasicPattern(new BasicPattern());
		ExprList someList = SSE.parseExprList(
				"((eq ?s 2) (<= ?s 4) (>= 5 ?s) (< ?t 4) (regex ?s \"foo\"))");
		fbp.addExprList(someList);
		Var s = Var.alloc("s"), t = Var.alloc("t");
		assertEquals(2, fbp.constraintsOn(s).size());
		assertEquals(1, fbp.constraintsOn(t).size());
	}

}
