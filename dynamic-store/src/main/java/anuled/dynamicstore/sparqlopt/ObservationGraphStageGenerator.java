package anuled.dynamicstore.sparqlopt;

import java.util.ArrayList;
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
// XXX: Oh god I think I'm registering this in a static initialiser somewhere. I
// tried all JUnit tests and only testQuery failed, but it won't fail in
// isolation. Evidently there's an import somewhere which is screwing things up.
public class ObservationGraphStageGenerator implements StageGenerator {
	StageGenerator nextStage = null;

	public ObservationGraphStageGenerator(StageGenerator nextStage) {
		this.nextStage = nextStage;
	}

	/** Naively matches against an iterable of triples */
	protected static QueryIterator chainTriples(List<Triple> triples,
			QueryIterator iter, ExecutionContext ctx) {
		return QueryIterBlockTriples.create(iter, BasicPattern.wrap(triples),
				ctx);
	}

	/**
	 * Represents a block of triple patterns in which all patterns are either of
	 * the form <code>?var :p :o</code> (i.e. variable, concrete, concrete) with
	 * the same variable each time, or no triples are of that form.
	 */
	protected static class TripleBlock {
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
	protected static List<TripleBlock> partitionBlocks(List<Triple> pattern) {
		Node currentVar = null;
		List<Triple> currentBlock = new ArrayList<>();
		List<TripleBlock> allBlocks = new ArrayList<>();
		for (Triple trip : pattern) {
			Node subj = trip.getSubject();
			if (subj.isVariable() && trip.getPredicate().isConcrete()
					&& trip.getObject().isConcrete()) {
				if (currentVar == null || !currentVar.equals(subj)) {
					if (!currentBlock.isEmpty()) {
						// end block
						TripleBlockType nextType = currentVar == null
								? TripleBlockType.ARBITRARY_BLOCK
								: TripleBlockType.VARIABLE_PATTERN_BLOCK;
						allBlocks.add(new TripleBlock(nextType, currentBlock));
						currentBlock = new ArrayList<>();
					}
					currentVar = subj;
				}
				currentBlock.add(trip);
			} else {
				// Not of the special "?var :concrete :concrete" form that we
				// look for
				if (currentVar != null && !currentBlock.isEmpty()) {
					allBlocks.add(new TripleBlock(
							TripleBlockType.VARIABLE_PATTERN_BLOCK,
							currentBlock));
					currentBlock = new ArrayList<>();
				}
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

		// Java almost has first class functions. Almost :(
/*		ConstraintFunction constraintsOn;
		if (pattern instanceof FilteredBasicPattern) {
			constraintsOn = ((FilteredBasicPattern) pattern)::constraintsOn;
		} else {
			constraintsOn = v -> Collections.emptySet();
		}*/

		ObservationGraph obsGraph = (ObservationGraph) graph;

		ArrayList<Triple> newTrips = new ArrayList<>();
		newTrips.addAll(pattern.getList());
		newTrips.sort(new TripleComparator());
		QueryIterator finalIter = input;
		for (TripleBlock block : partitionBlocks(newTrips)) {
			assert block.pattern.size() > 0;
			switch (block.type) {
			case VARIABLE_PATTERN_BLOCK:
				finalIter = new VariableBlockHandler(finalIter, execCtx,
						block.pattern, obsGraph);
				break;
			case ARBITRARY_BLOCK:
				finalIter = chainTriples(block.pattern, finalIter, execCtx);
				break;
			}
		}
		return finalIter;
	}
}
