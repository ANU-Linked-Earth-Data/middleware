package anuled.dynamicstore;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import anuled.vocabulary.LED;

public class TestLandsatGraphAssembler {
	private static TestData td;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		td = new TestData();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		td.dispose();
	}

	@Test(timeout=60000)
	public void testAssembler() throws IOException {
		LandsatGraphAssembler.init();
		Model spec = ModelFactory.createDefaultModel();
		InputStream stream = TestLandsatGraphAssembler.class
				.getResourceAsStream("/simple-landsat-graph.ttl");
		spec.read(stream, null, "TTL");
		stream.close();
		// Correct path
		Resource graphDef = spec.getResource(spec.expandPrefix(":testGraph"));
		graphDef.removeAll(LED.hdf5Path);
		graphDef.addLiteral(LED.hdf5Path, td.getPath());
		Resource root = spec.createResource(spec.expandPrefix(":testDataset"));
		Model model = ((Dataset) Assembler.general.open(root)).getDefaultModel();
		assertTrue(model.listStatements().hasNext());
	}

}
