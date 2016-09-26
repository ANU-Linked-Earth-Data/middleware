package anuled.dynamicstore.sparqlopt;

import static org.junit.Assert.*;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.impl.CollectionGraph;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphOne;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterRoot;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.junit.Test;

public class TestObservationGraphOpExecutorFactory {

	@Test
	public void testExecuteOpFilterQueryIterator() {
		// jump through hoops to satisfy Jena
		Graph blankGraph = new CollectionGraph();
		DatasetGraph dsGraph = new DatasetGraphOne(blankGraph);
		ExecutionContext ctx = new ExecutionContext(new Context(), blankGraph,
				dsGraph, OpExecutor.stdFactory);

		ObservationGraphOpExecutorFactory ogFactory = new ObservationGraphOpExecutorFactory(
				ctx);
		OpFilter bgpFilter = (OpFilter) SSE
				.parseOp("(filter (< ?o 42) (bgp (?s ?p ?o)))"),
				fakeFilter = OpFilter.filter(OpNull.create());

		// Should execute the same regardless of whether there's a BGP
		QueryIterator rootIter = QueryIterRoot.create(ctx);
		QueryIterator result = ogFactory.execute(fakeFilter, rootIter);
		assertFalse(result.hasNext());

		// This has an inner BGP (rather than some fake block)
		rootIter = QueryIterRoot.create(ctx);
		result = ogFactory.execute(bgpFilter, rootIter);
		assertFalse(result.hasNext());
	}

}
