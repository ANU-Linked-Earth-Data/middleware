package anuled.dynamicstore.sparqlopt;

import java.util.Comparator;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/**
 * Comparator which orders triples by subject, then object, then predicate.
 * Sub-ordering is by whether node is concrete (in which case it comes
 * first), then by variable name (if applicable).
 */
class TripleComparator implements Comparator<Triple> {
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