package anuled.dynamicstore;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import anuled.vocabulary.LED;

public class TestQBCovGraphAssembler {
	private static TestData td;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		td = new TestData();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		td.dispose();
	}

	@Test(timeout = 60000)
	public void testAssembler() throws IOException {
		QBCovGraphAssembler.init();
		innerTest();
		QBCovGraphAssembler.init();
		QBCovGraphAssembler.init();
		innerTest();
	}

	private void innerTest() throws IOException {
		Model spec = ModelFactory.createDefaultModel();
		InputStream stream = TestQBCovGraphAssembler.class
				.getResourceAsStream("/simple-landsat-graph.ttl");
		spec.read(stream, null, "TTL");
		stream.close();
		// Correct path
		Resource graphDef = spec.getResource(spec.expandPrefix(":testGraph"));
		graphDef.removeAll(LED.hdf5Path);
		graphDef.addLiteral(LED.hdf5Path, td.getPath());
		Resource root = spec.createResource(spec.expandPrefix(":testDataset"));
		Model model = ((Dataset) Assembler.general.open(root))
				.getDefaultModel();
		assertTrue(model.listStatements().hasNext());
		
		// try with two HDF5 paths
		graphDef.addLiteral(LED.hdf5Path, "some other path");
		boolean gotException = false;
		try {
			Assembler.general.open(root);
		} catch (AssemblerException e) {
			gotException = true;
		}
		assertTrue(gotException);

		// now try it again without any path
		graphDef.removeAll(LED.hdf5Path);
		graphDef.removeAll(LED.hdf5Path);
		gotException = false;
		try {
			Assembler.general.open(root);
		} catch (AssemblerException e) {
			gotException = true;
		}
		assertTrue(gotException);
	}

}
