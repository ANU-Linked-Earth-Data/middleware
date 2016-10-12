package anuled.dynamicstore.sparqlopt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterBlockTriples;
import org.apache.jena.sparql.engine.main.StageGenerator;

import anuled.dynamicstore.ObservationGraph;
import anuled.dynamicstore.Util;
import anuled.dynamicstore.rdfmapper.properties.LatLonBoxProperty.BoundType;
import anuled.dynamicstore.rdfmapper.properties.LatMaxProperty;
import anuled.dynamicstore.rdfmapper.properties.LatMinProperty;
import anuled.dynamicstore.rdfmapper.properties.LongMaxProperty;
import anuled.dynamicstore.rdfmapper.properties.LongMinProperty;
import anuled.dynamicstore.rdfmapper.properties.ObservationProperty;
import anuled.dynamicstore.rdfmapper.properties.PropertyIndex;

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

	/** typedef to clean up some code below */
	protected static class PropertyMapping
			extends LazyMap<Pair<Var, ObservationProperty>, Set<Var>> {
		protected PropertyMapping() {
			super(new HashMap<>(), k -> new HashSet<>());
		}

		private static final long serialVersionUID = 6122053846597081334L;
	}

	/**
	 * Scans for triples of the form </code>?s led:* ?o</code>. Will produce a
	 * map which enables lookup of objects in triples of that form based on the
	 * subject and property.
	 * 
	 * This process is mainly useful when there's an enclosing
	 * <code>FILTER()</code> which is trying to add a bounding box constraint to
	 * the query. In that case, the middleware is interested in cases where the
	 * predicate is <code>led:{lat,long}{Min,Max}</code>.
	 */
	protected static PropertyMapping associatedProperties(
			List<Triple> triples) {
		PropertyMapping rv = new PropertyMapping();
		for (Triple t : triples) {
			Node subjNode = t.getSubject();
			if (!subjNode.isVariable()) {
				continue;
			}
			Var subjVar = (Var) subjNode;

			Node predNode = t.getPredicate();
			if (!predNode.isURI()) {
				continue;
			}
			Optional<ObservationProperty> optProp = PropertyIndex
					.getProperty(predNode.getURI());
			if (!optProp.isPresent()) {
				continue;
			}
			ObservationProperty prop = optProp.get();

			Node objNode = t.getObject();
			if (!objNode.isVariable()) {
				continue;
			}
			Var objVar = (Var) objNode;

			Pair<Var, ObservationProperty> key = Pair.of(subjVar, prop);
			rv.get(key).add(objVar);
		}
		return rv;
	}

	/**
	 * Converts inequality constraints on an observation property to a triple
	 * constraining the corresponding subject. Example: if you have an
	 * inequality constraint of the form <code>?v < 42</code>, and a triple
	 * pattern of the form <code>?s led:latMax ?v</code>, you might call
	 * <code>constraintToTriple(?v < 13, ?s, LatMaxProperty)</code>.
	 * 
	 * Note that the var in the constraint may be different from the var passed
	 * explicitly: the explicitly passed var should be the subject of the
	 * <code>?s :prop ?v</code> triple, and the var in the inequality constraint
	 * will likely be the object (which can be constrained with a filter).
	 */
	protected static Optional<Triple> constraintToTriple(
			InequalityConstraint constraint, Var var,
			ObservationProperty prop) {
		if (!(constraint.leftIsVar() ^ constraint.rightIsVar())) {
			// Can't do anything for var/var or const/const bindings.
			// TODO: Should be able to take a binding to resolve problems
			// like this (potentially).
			return Optional.empty();
		}
		// TODO: Log warning if the thing above isn't true.

		// check whether the given property is measuring lat or lon
		boolean isLat;
		if (prop instanceof LatMaxProperty || prop instanceof LatMinProperty) {
			isLat = true;
		} else if (prop instanceof LongMaxProperty
				|| prop instanceof LongMinProperty) {
			isLat = false;
		} else {
			return Optional.empty();
		}
		// check whether the given property is measuring min or max
		boolean propIsMax = prop instanceof LatMaxProperty
				|| prop instanceof LongMaxProperty;

		boolean constraintIsMax;
		Node constraintNode;
		if (constraint.leftIsVar()) {
			// var <= constant; we need a maximum (upper bound) constraint
			constraintIsMax = true;
			constraintNode = constraint.getRight();
		} else {
			// constant <= var; we need a minimum (lower bound) constraint
			assert constraint.rightIsVar();
			constraintIsMax = false;
			constraintNode = constraint.getLeft();
		}

		// we can only upper bound maximum properties (LatMax, LongMax) and
		// lower bound minimum properties (LatMin, LongMin); if we're asked to
		// do anything else, we'll have to return Optional.empty()
		if (propIsMax != constraintIsMax) {
			return Optional.empty();
		}

		// Now we can actually create the LatLonBoxProperty
		BoundType type;
		if (constraintIsMax) {
			if (isLat) {
				type = BoundType.BoxTop;
			} else {
				type = BoundType.BoxRight;
			}
		} else {
			if (isLat) {
				type = BoundType.BoxBottom;
			} else {
				type = BoundType.BoxLeft;
			}
		}

		return Optional.of(new Triple(var,
				Util.createURINode(type.getURI()), constraintNode));
	}

	/**
	 * Generate constraints (presumably from a higher-level <code>FILTER</code>)
	 * for a triple pattern. Constraints are encoded as new triples so that
	 * <code>VariableBlockHandler</code> can handle them transparently.
	 */
	protected static List<Triple> makeNewConstraints(PropertyMapping propMap,
			ConstraintFunction constraintsOn) {
		List<Triple> rv = new ArrayList<>();
		for (Entry<Pair<Var, ObservationProperty>, Set<Var>> entry : propMap
				.entrySet()) {
			Pair<Var, ObservationProperty> key = entry.getKey();
			for (Var otherVar : entry.getValue()) {
				for (InequalityConstraint constraint : constraintsOn
						.apply(otherVar)) {
					constraintToTriple(constraint, key.getLeft(),
							key.getRight()).ifPresent(trip -> rv.add(trip));
				}
			}
		}
		return rv;
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
		ConstraintFunction constraintsOn;
		if (pattern instanceof FilteredBasicPattern) {
			constraintsOn = ((FilteredBasicPattern) pattern)::constraintsOn;
		} else {
			constraintsOn = v -> Collections.emptySet();
		}

		ObservationGraph obsGraph = (ObservationGraph) graph;

		PropertyMapping propMap = associatedProperties(pattern.getList());
		List<Triple> extraConstraints = makeNewConstraints(propMap,
				constraintsOn);

		// choose a sane triple ordering; this is required by the
		// partitionBlocks function
		ArrayList<Triple> newTrips = new ArrayList<>();
		newTrips.addAll(pattern.getList());
		newTrips.addAll(extraConstraints);
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
