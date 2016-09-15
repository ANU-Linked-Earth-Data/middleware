package anuled.dynamicstore.sparqlopt;

import static org.junit.Assert.*;
import static org.apache.jena.graph.NodeFactory.*;
import static anuled.dynamicstore.sparqlopt.ObservationGraphStageGenerator.partitionBlocks;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

import anuled.dynamicstore.sparqlopt.ObservationGraphStageGenerator.TripleBlock;
import anuled.dynamicstore.sparqlopt.ObservationGraphStageGenerator.TripleBlockType;
import anuled.dynamicstore.util.JenaUtil;
import anuled.vocabulary.LED;
import anuled.vocabulary.QB;

// I'm genuinely proud of the noun pile that is the name of this class.
public class ObservationGraphStageGeneratorTest {

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
		Arrays.sort(unordered,
				new ObservationGraphStageGenerator.TripleComparator());
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
		assertEquals(TripleBlockType.VARIABLE_PATTERN_BLOCK, result.get(1).type);
		assertEquals(2, result.get(1).pattern.size());
		assertEquals(TripleBlockType.VARIABLE_PATTERN_BLOCK, result.get(2).type);
		assertEquals(1, result.get(2).pattern.size());
		assertEquals(TripleBlockType.ARBITRARY_BLOCK, result.get(3).type);
		assertEquals(2, result.get(3).pattern.size());
		
	}

	@Test
	public void testExecute() {

	}

}
