package anuled.dynamicstore.sparqlopt;

import static anuled.dynamicstore.rdfmapper.properties.LatLonBoxProperty.BoundType.*;
import static anuled.dynamicstore.sparqlopt.ConstraintType.*;
import static anuled.dynamicstore.sparqlopt.ObservationGraphStageGenerator.*;
import static anuled.dynamicstore.util.JenaUtil.*;
import static org.apache.jena.graph.NodeFactory.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterRoot;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.vocabulary.RDF;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import anuled.dynamicstore.ObservationGraph;
import anuled.dynamicstore.ObservationNode;
import anuled.dynamicstore.TestData;
import anuled.dynamicstore.rdfmapper.properties.BandProperty;
import anuled.dynamicstore.rdfmapper.properties.LatMaxProperty;
import anuled.dynamicstore.rdfmapper.properties.LatMinProperty;
import anuled.dynamicstore.rdfmapper.properties.LongMaxProperty;
import anuled.dynamicstore.rdfmapper.properties.LongMinProperty;
import anuled.dynamicstore.rdfmapper.properties.ObservationProperty;
import anuled.dynamicstore.rdfmapper.properties.PropertyIndex;
import anuled.dynamicstore.sparqlopt.ObservationGraphStageGenerator.PropertyMapping;
import anuled.dynamicstore.sparqlopt.ObservationGraphStageGenerator.TripleBlock;
import anuled.dynamicstore.util.JenaUtil;
import anuled.vocabulary.LED;
import anuled.vocabulary.QB;

// I'm genuinely proud of the noun pile that is the name of this class.
public class TestObservationGraphStageGenerator {
	private ObservationGraph graph;
	private static TestData td;
	ObservationProperty latMinProp, latMaxProp, longMinProp, longMaxProp,
			irrelevantProp;
	Node boxBottom, boxTop, boxLeft, boxRight;

	@BeforeClass
	public static void setUpClass() throws IOException {
		td = new TestData();
	}

	@AfterClass
	public static void tearDownClass() {
		td.dispose();
	}

	@Before
	public void setUp() throws Exception {
		graph = new ObservationGraph(td.getPath(), "http://example.com/fakeDS");
		latMinProp = new LatMinProperty();
		latMaxProp = new LatMaxProperty();
		longMinProp = new LongMinProperty();
		longMaxProp = new LongMaxProperty();
		irrelevantProp = new BandProperty();
		boxBottom = createURINode(BoxBottom.getURI());
		boxTop = createURINode(BoxTop.getURI());
		boxLeft = createURINode(BoxLeft.getURI());
		boxRight = createURINode(BoxRight.getURI());
	}

	@Test
	public void testTripleSorting() {
		// Test the triple comparator. Instead of testing at a low level, we'll
		// just use it to sort something (isn't that what we want anyway?).
		Triple[] expected = {
				new Triple(createBlankNode(), createBlankNode(),
						createBlankNode()),
				new Triple(createBlankNode(), createBlankNode(),
						createVariable("aardvark")),
				new Triple(createBlankNode(), createBlankNode(),
						createVariable("bar")),
				new Triple(createVariable("bar"), createVariable("foo"),
						createURI("http://example.com")),
				new Triple(createVariable("foo"), createVariable("bar"),
						createVariable("baz")) };
		Triple[] unordered = { expected[2], expected[1], expected[4],
				expected[0], expected[3] };
		Arrays.sort(unordered, new TripleComparator());
		assertEquals(expected.length, unordered.length);
		for (int i = 0; i < unordered.length; i++) {
			assertEquals(expected[i], unordered[i]);
		}
	}

	@Test
	public void testPartitionBlocks() {
		Var var1 = Var.alloc("varA"), var2 = Var.alloc("varB");
		Node fakeURI = JenaUtil.createURINode("http://fake/");
		Node lit13 = JenaUtil.createLiteralNode(13);
		Triple varConcBlock1 = new Triple(var1, LED.etmBand.asNode(), lit13),
				varConcBlock2 = new Triple(var1, QB.dataSet.asNode(), fakeURI),
				varConcBlock3 = new Triple(var2, RDF.type.asNode(),
						LED.GridSquare.asNode()),
				concConcBlock1 = new Triple(fakeURI, RDF.type.asNode(),
						LED.Pixel.asNode()),
				concConcBlock2 = new Triple(fakeURI, fakeURI, fakeURI),
				varVarBlock1 = new Triple(var1, var1, var1),
				varVarBlock2 = new Triple(var1, LED.bounds.asNode(), var1);

		// Empty block should be partitioned into something empty
		List<TripleBlock> result = partitionBlocks(Arrays.asList());
		assertEquals(0, result.size());

		// Single variable block (each triple has same variable as subject;
		// object and predicate are concrete)
		result = partitionBlocks(Arrays.asList(varConcBlock1, varConcBlock2));
		assertEquals(1, result.size());
		TripleBlock block = result.get(0);
		assertEquals(TripleBlockType.VARIABLE_PATTERN_BLOCK, block.type);
		assertEquals(2, block.pattern.size());

		// More complex example
		result = partitionBlocks(Arrays.asList(concConcBlock1, concConcBlock2,
				varConcBlock1, varConcBlock2, varConcBlock3, varVarBlock1,
				varVarBlock2));
		assertEquals(4, result.size());
		assertEquals(TripleBlockType.ARBITRARY_BLOCK, result.get(0).type);
		assertEquals(2, result.get(1).pattern.size());
		assertEquals(TripleBlockType.VARIABLE_PATTERN_BLOCK,
				result.get(1).type);
		assertEquals(2, result.get(1).pattern.size());
		assertEquals(TripleBlockType.VARIABLE_PATTERN_BLOCK,
				result.get(2).type);
		assertEquals(1, result.get(2).pattern.size());
		assertEquals(TripleBlockType.ARBITRARY_BLOCK, result.get(3).type);
		assertEquals(2, result.get(3).pattern.size());

	}

	@Test
	public void testExecute() {
		ObservationGraphStageGenerator gen = new ObservationGraphStageGenerator(
				null);
		ExecutionContext ctx = mock(ExecutionContext.class);
		when(ctx.getActiveGraph()).thenReturn(graph);
		QueryIterator root = QueryIterRoot.create(BindingFactory.binding(),
				ctx);
		QueryIterator result = gen.execute(BasicPattern.wrap(Arrays.asList()),
				root, ctx);
		assertTrue(result.hasNext());
		result.next();
		assertFalse(result.hasNext());

		// Just try to get a specific node this time
		Var obsVar = Var.alloc("s");
		List<Triple> pattern = Arrays.asList(
				new Triple(obsVar, LED.dggsCell.asNode(),
						JenaUtil.createLiteralNode("R78")),
				new Triple(obsVar, RDF.type.asNode(), LED.Pixel.asNode()),
				new Triple(obsVar, LED.etmBand.asNode(),
						JenaUtil.createLiteralNode(3)));
		root = QueryIterRoot.create(BindingFactory.binding(), ctx);
		result = gen.execute(BasicPattern.wrap(pattern), root, ctx);
		assertTrue(result.hasNext());
		Binding outBinding = result.next();
		assertTrue(outBinding.contains(obsVar));
		Node outURI = outBinding.get(obsVar);
		assertTrue(outURI.isURI());
		assertTrue(outURI instanceof ObservationNode);
		assertEquals(
				"https://anulinkedearth.org/rdf/observation/2013/05/27/23/58/20/"
						+ "cell/R78/levelSquare-3/levelPixel-3/band-3",
				outURI.getURI());
		assertFalse(result.hasNext());

		// What happens when we have an existing node binding?
		// If it's compatible with the results we have, we should still get a
		// binding back...
		root = QueryIterSingleton.create(BindingFactory.binding(obsVar, outURI),
				ctx);
		result = gen.execute(BasicPattern.wrap(pattern), root, ctx);
		assertTrue(result.hasNext());
		outBinding = result.next();
		assertFalse(result.hasNext());
		assertEquals(outURI, outBinding.get(obsVar));

		// Otherwise, we should get nothing.
		root = QueryIterSingleton.create(BindingFactory.binding(obsVar,
				JenaUtil.createURINode("http://fake/")), ctx);
		result = gen.execute(BasicPattern.wrap(pattern), root, ctx);
		assertFalse(result.hasNext());
	}

	@Test
	public void testAssociatedProperties() {
		List<Triple> emptyList = Collections.emptyList();
		assertEquals(0, associatedProperties(emptyList).size());

		Var v1 = Var.alloc("v1"), v2 = Var.alloc("v2"), v3 = Var.alloc("v3");
		Node uri1 = LED.etmBand.asNode(), uri2 = RDF.type.asNode(),
				uri3 = LED.latMin.asNode(),
				uri4 = createURINode("http://not-a-property/");
		Node blank1 = createBlankNode();
		List<Triple> bigList = Arrays.asList(new Triple(v1, v2, v3),
				new Triple(uri1, uri2, v1), new Triple(v1, uri1, v2),
				new Triple(v1, uri1, v3), new Triple(v1, uri2, v3),
				new Triple(v1, blank1, v2), new Triple(v3, uri3, v1),
				new Triple(v3, uri2, v2), new Triple(v2, uri4, v1));
		// Pairs we want to look for:
		// 1) (v1, etmBand) (x2)
		// 2) (v1, type) (x1)
		// 3) (v3, latMin) (x1)
		// 4) (v3, type) (x1)
		PropertyMapping props = associatedProperties(bigList);
		assertEquals(4, props.size());
		Function<Node, ObservationProperty> toProp = node -> PropertyIndex
				.getProperty(node.getURI()).get();
		assertEquals(2, props.get(Pair.of(v1, toProp.apply(uri1))).size());
		assertEquals(1, props.get(Pair.of(v1, toProp.apply(uri2))).size());
		assertEquals(1, props.get(Pair.of(v3, toProp.apply(uri3))).size());
		assertEquals(1, props.get(Pair.of(v3, toProp.apply(uri2))).size());
	}

	@Test
	public void testConstraintToTriple() {
		Var v1 = Var.alloc("v1"), v2 = Var.alloc("v2"), v3 = Var.alloc("v3");
		Node const1 = createLiteralNode(42), const2 = createLiteralNode(13);
		InequalityConstraint doubleVarConstraint = new InequalityConstraint(v1,
				v2, LESS),
				doubleLitConstraint = new InequalityConstraint(const1, const2,
						LESS_EQ),
				leftVarConstraint = new InequalityConstraint(v1, const1,
						LESS_EQ),
				rightVarConstraint = new InequalityConstraint(const2, v2, LESS);

		// Can't generate constraints here because there's either too few or too
		// many variables.
		assertFalse(constraintToTriple(doubleVarConstraint, v3, latMinProp)
				.isPresent());
		assertFalse(constraintToTriple(doubleLitConstraint, v1, latMaxProp)
				.isPresent());

		// Can't generate constraints here because we're trying to upper bound a
		// minimum property (not supported) or lower bound a maximum property
		// (again, not supported)---we only support lower bounding minima and
		// upper bounding maxima.
		assertFalse(constraintToTriple(leftVarConstraint, v1, latMinProp)
				.isPresent());
		assertFalse(constraintToTriple(leftVarConstraint, v2, longMinProp)
				.isPresent());
		assertFalse(constraintToTriple(rightVarConstraint, v3, latMaxProp)
				.isPresent());
		assertFalse(constraintToTriple(rightVarConstraint, v1, latMaxProp)
				.isPresent());

		// This isn't latMin/latMax so it can't generate anything
		assertFalse(constraintToTriple(rightVarConstraint, v1, irrelevantProp)
				.isPresent());

		// The following constraints should be all be okay
		assertEquals(new Triple(v3, boxBottom, const2),
				constraintToTriple(rightVarConstraint, v3, latMinProp).get());
		assertEquals(new Triple(v1, boxLeft, const2),
				constraintToTriple(rightVarConstraint, v1, longMinProp).get());
		assertEquals(new Triple(v2, boxTop, const1),
				constraintToTriple(leftVarConstraint, v2, latMaxProp).get());
		assertEquals(new Triple(v3, boxRight, const1),
				constraintToTriple(leftVarConstraint, v3, longMaxProp).get());
	}

	@Test
	public void testMakeNewConstraints() {
		Var v1 = Var.alloc("v1"), v2 = Var.alloc("v2"), v3 = Var.alloc("v3");
		Node const1 = createLiteralNode(42), const2 = createLiteralNode(13);

		// Now make the mapping
		PropertyMapping props = new PropertyMapping();
		props.get(Pair.of(v1, latMinProp)).add(v2);
		props.get(Pair.of(v1, latMinProp)).add(v3);
		props.get(Pair.of(v1, latMaxProp)).add(v2);
		props.get(Pair.of(v2, longMinProp)).add(v1);
		props.get(Pair.of(v3, irrelevantProp)).add(v2);
		ConstraintFunction constraintsOn = var -> {
			Set<InequalityConstraint> rv = new HashSet<>();
			if (v1.equals(var)) {
				rv.add(new InequalityConstraint(const1, v1, LESS));
			} else if (v2.equals(var)) {
				rv.add(new InequalityConstraint(const1, v2, LESS_EQ));
				rv.add(new InequalityConstraint(v3, const1, LESS_EQ));
			} else if (v3.equals(var)) {
				rv.add(new InequalityConstraint(const2, v3, LESS));
			}
			return rv;
		};

		// Finally, check that we get the expected constraint set from the
		// mapping
		Set<Triple> constraints = new HashSet<>(
				makeNewConstraints(props, constraintsOn));
		assertEquals(4, constraints.size());
		Set<Triple> expectedConstraints = new HashSet<>(
				Arrays.asList(new Triple(v1, boxBottom, const1),
						new Triple(v1, boxBottom, const2),
						new Triple(v1, boxTop, const1),
						new Triple(v2, boxLeft, const1)));
		assertEquals(expectedConstraints, constraints);
	}
}
