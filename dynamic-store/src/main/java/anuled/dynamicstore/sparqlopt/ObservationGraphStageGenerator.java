package anuled.dynamicstore.sparqlopt;

import java.util.ArrayList;
import java.util.Comparator;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
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
			int predCmp = compareNode(left.getPredicate(), right.getPredicate());
			if (predCmp != 0) {
				return predCmp;
			}
			return compareNode(left.getObject(), right.getObject());
		}
	}

	@Override
	public QueryIterator execute(BasicPattern pattern, QueryIterator input,
			ExecutionContext execCtx) {
		Graph graph = execCtx.getActiveGraph();
		if (!(graph instanceof ObservationGraph)) {
			return nextStage.execute(pattern, input, execCtx);
		}
		// All we'll do for now is reorder triples in the pattern.
		ArrayList<Triple> newTrips = new ArrayList<>();
		newTrips.addAll(pattern.getList());
		newTrips.sort(new TripleComparator());
		BasicPattern newPattern = BasicPattern.wrap(newTrips);
		QueryIterator out = nextStage.execute(newPattern, input, execCtx);
		return out;
	}
}
