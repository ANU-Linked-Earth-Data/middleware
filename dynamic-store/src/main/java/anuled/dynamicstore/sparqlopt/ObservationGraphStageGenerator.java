package anuled.dynamicstore.sparqlopt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterBlockTriples;
import org.apache.jena.sparql.engine.main.StageGenerator;

import anuled.dynamicstore.ObservationGraph;

/**
 * Jena <code>StageGenerator</code> which handles BGP matching for
 * <code>ObservationGraph</code>s intelligently.
 */
public class ObservationGraphStageGenerator implements StageGenerator {
	StageGenerator nextStage = null;

	public ObservationGraphStageGenerator(StageGenerator nextStage) {
		this.nextStage = nextStage;
	}

	/**
	 * Comparator which orders triples by subject, then object, then predicate.
	 * Sub-ordering is by whether node is concrete (in which case it comes
	 * first), then by variable name (if applicable).
	 */
	protected static class TripleComparator implements Comparator<Triple> {
		protected int compareNode(Node left, Node right) {
			if (left.isConcrete() && !right.isConcrete()) {
				// put left node first
				return -1;
			} else if (!left.isConcrete() && right.isConcrete()) {
				// put right node first
				return 1;
			}
			if (left.isVariable() && right.isVariable()) {
				// first node is the one that comes first lexicographically
				return left.getName().compareTo(right.getName());
			}
			// we don't care how nodes are ordered
			return 0;
		}

		@Override
		public int compare(Triple left, Triple right) {
			int subjCmp = compareNode(left.getSubject(), right.getSubject());
			if (subjCmp != 0) {
				return subjCmp;
			}
			int predCmp = compareNode(left.getPredicate(),
					right.getPredicate());
			if (predCmp != 0) {
				return predCmp;
			}
			return compareNode(left.getObject(), right.getObject());
		}
	}

	/** Naively matches against an iterable of triples */
	protected QueryIterator chainTriples(List<Triple> triples,
			QueryIterator iter, ExecutionContext ctx) {
		return QueryIterBlockTriples.create(iter, BasicPattern.wrap(triples),
				ctx);
	}

	/** Intelligently handle <code>?var :concrete :concrete</code> blocks. */
	protected QueryIterator handleVariableBlock(List<Triple> triples,
			QueryIterator iter, ExecutionContext ctx) {
		if (triples.size() == 0) {
			return iter;
		}
		
		// Plan:
		// 1) Check whether there's a binding for the current
		//   variable in the input QueryIterator.
		// 2) If there is an existing binding for the variable
		//    we care about, we can just chainTriples normally.
		// 3) Otherwise, we can use QueryIterExtendByVar on
		//    each binding.s
//		Triple firstTrip = triples.get(0);
//		String varName = firstTrip.getSubject().getName();
//		Var;
//		if (iter.hasNext()) {
//			Binding nextBinding = iter.nextBinding();
//			nextBinding.contains
//		}
//		// Constrain subject filter
//		for (Triple trip : triples) {
//
//		}
		return chainTriples(triples, iter, ctx);
	}

	protected enum TripleBlockType {
		VARIABLE_PATTERN_BLOCK, ARBITRARY_BLOCK
	}

	/**
	 * Represents a block of triple patterns in which all patterns are either of
	 * the form <code>?var :p :o</code> (i.e. variable, concrete, concrete) with
	 * the same variable each time, or no triples are of that form.
	 */
	protected class TripleBlock {
		public final List<Triple> pattern;
		public TripleBlockType type;

		TripleBlock(TripleBlockType type, List<Triple> pattern) {
			this.type = type;
			this.pattern = pattern;
		}
	}

	/**
	 * Groups blocks according to whether they follow the magical
	 * <code>?var :p :o</code> form or not. Blocks following the magical form
	 * have the same variable name across all triples in the block. This method
	 * assumes that the input list has been sorted so that such blocks appear
	 * adjacent to one another.
	 */
	protected Iterable<TripleBlock> partitionBlocks(List<Triple> pattern) {
		Node currentVar = null;
		List<Triple> currentBlock = new ArrayList<>();
		List<TripleBlock> allBlocks = new ArrayList<>();
		for (Triple trip : pattern) {
			Node subj = trip.getSubject();
			if (subj.isVariable() && trip.getPredicate().isConcrete()
					&& trip.getObject().isConcrete()) {
				if (currentVar == null || !currentVar.equals(subj)) {
					// end block
					TripleBlockType nextType = currentVar == null
							? TripleBlockType.ARBITRARY_BLOCK
							: TripleBlockType.VARIABLE_PATTERN_BLOCK;
					allBlocks.add(new TripleBlock(nextType, currentBlock));
					currentBlock = new ArrayList<>();
					currentVar = subj;
				}
				currentBlock.add(trip);
			} else {
				// Not of the special "?var :concrete :concrete" form that we
				// look for
				currentVar = null;
				currentBlock.add(trip);
			}
		}
		if (!currentBlock.isEmpty()) {
			TripleBlockType nextType = currentVar == null
					? TripleBlockType.ARBITRARY_BLOCK
					: TripleBlockType.VARIABLE_PATTERN_BLOCK;
			allBlocks.add(new TripleBlock(nextType, currentBlock));
		}
		return allBlocks;
	}

	@Override
	public QueryIterator execute(BasicPattern pattern, QueryIterator input,
			ExecutionContext execCtx) {
		Graph graph = execCtx.getActiveGraph();
		if (!(graph instanceof ObservationGraph)) {
			return nextStage.execute(pattern, input, execCtx);
		}
		// All we'll do for now is reorder triples in the pattern.
		// Check out
		// https://github.com/apache/jena/blob/master/jena-arq/src-examples/arq/examples/bgpmatching/StageGeneratorAlt.java
		// for a cool example of chaining through a pattern.
		ArrayList<Triple> newTrips = new ArrayList<>();
		newTrips.addAll(pattern.getList());
		newTrips.sort(new TripleComparator());
		QueryIterator finalIter = input;
		for (TripleBlock block : partitionBlocks(newTrips)) {
			switch (block.type) {
			case VARIABLE_PATTERN_BLOCK:
				finalIter = handleVariableBlock(block.pattern, finalIter,
						execCtx);
				break;
			case ARBITRARY_BLOCK:
				finalIter = chainTriples(block.pattern, finalIter, execCtx);
				break;
			default:
				throw new RuntimeException("Shouldn't get here :/");
			}
		}
		return chainTriples(newTrips, input, execCtx);
	}
}
