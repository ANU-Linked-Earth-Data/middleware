package anuled.dynamicstore;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
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
		Resource dsDef = spec.getResource(spec.expandPrefix(":qbCovDataset"));
		dsDef.removeAll(LED.hdf5Path);
		dsDef.addLiteral(LED.hdf5Path, td.getPath());
		Resource root = spec.createResource(spec.expandPrefix(":testDataset"));
		Dataset ds = (Dataset) Assembler.general.open(root);
		assertFalse(ds.getDefaultModel().listStatements().hasNext());
		int numNamedSets = 0;
		Iterator<String> nameIter = ds.listNames();
		while (nameIter.hasNext()) {
			nameIter.next();
			numNamedSets++;
		}
		assertEquals(2, numNamedSets);
		for (String graphName : new String[] { ":testObsGraph",
				":testMetaGraph" }) {
			assertTrue(ds.getNamedModel(spec.expandPrefix(graphName))
					.listStatements().hasNext());
		}

		// try with two HDF5 paths
		dsDef.addLiteral(LED.hdf5Path, "some other path");
		boolean gotException = false;
		try {
			Assembler.general.open(root);
		} catch (AssemblerException e) {
			gotException = true;
		}
		assertTrue(gotException);

		// now try it again without any path
		dsDef.removeAll(LED.hdf5Path);
		dsDef.removeAll(LED.hdf5Path);
		gotException = false;
		try {
			Assembler.general.open(root);
		} catch (AssemblerException e) {
			gotException = true;
		}
		assertTrue(gotException);
	}

}
