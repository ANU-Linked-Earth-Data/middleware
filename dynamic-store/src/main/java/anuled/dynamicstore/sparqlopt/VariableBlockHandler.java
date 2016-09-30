package anuled.dynamicstore.sparqlopt;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterExtendByVar;
import org.apache.jena.sparql.engine.iterator.QueryIterRepeatApply;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;

import anuled.dynamicstore.ObservationGraph;

/** Intelligently handle <code>?var :concrete :concrete</code> blocks. */
class VariableBlockHandler extends QueryIterRepeatApply {
	ObservationGraph graph;
	List<Triple> triples;
	ConstraintFunction constraintsOn;
	Var newVar;

	public VariableBlockHandler(QueryIterator input,
			ExecutionContext context, List<Triple> triples,
			ObservationGraph graph, ConstraintFunction constraintsOn) {
		super(input, context);
		this.graph = graph;
		this.triples = triples;
		assert !triples.isEmpty();
		Triple firstTrip = triples.get(0);
		Node subj = firstTrip.getSubject();
		assert subj.isVariable();
		newVar = Var.alloc(subj);
		this.constraintsOn = constraintsOn;
	}

	@Override
	protected QueryIterator nextStage(Binding binding) {
		if (binding.contains(newVar)) {
			// there's already a binding for the variable we care about in
			// the parent (!!)
			QueryIterator single = QueryIterSingleton.create(binding,
					getExecContext());
			return ObservationGraphStageGenerator.chainTriples(triples, single, getExecContext());
		}
		// Don't worry about the cast! observationURIs returns a stream of
		// ObservationNodes, and ObservationNode is a Node subclass.
		Iterator<Node> obsURIBindings = graph.observationURIs(triples)
				.map(n -> (Node) n).iterator();
		return new QueryIterExtendByVar(binding, newVar, obsURIBindings,
				getExecContext());
	}

}