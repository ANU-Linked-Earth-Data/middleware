package anuled.dynamicstore.rdfmapper.properties;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import anuled.dynamicstore.TestData;
import anuled.dynamicstore.backend.Cell;
import anuled.dynamicstore.backend.HDF5Dataset;
import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.backend.PixelObservation;
import anuled.dynamicstore.backend.TileObservation;
import anuled.vocabulary.LED;
import anuled.vocabulary.QB;

public class TestGetProperties {
	private HDF5Dataset ds;
	private static TestData td;

	@BeforeClass
	public static void setUpClass() throws IOException {
		td = new TestData();
	}

	@AfterClass
	public static void tearDownClass() {
		td.dispose();
	}

	@Before
	public void setUp() {
		ds = new HDF5Dataset(td.getPath());
	}

	@After
	public void tearDown() {
		ds.dispose();
	}
	
	private List<Node> getProp(Resource prop, Observation obs) {
		return getProp(prop.getURI(), obs);
	}
	
	private List<Node> getProp(String prop, Observation obs) {
		ObservationProperty fetcher = PropertyIndex.getProperty(prop);
		assertNotNull(fetcher);
		return fetcher.valuesForObservation(obs).collect(Collectors.toList());
	}

	@Test
	public void testGetType() {
		Cell cell = ds.dggsCell("R7852");
		PixelObservation pxObs = cell.pixelObservation(4);
		TileObservation tlObs = cell.tileObservation(4);
		List<Node> pxProps = getProp(RDF.type, pxObs);
		List<Node> tlProps = getProp(RDF.type, tlObs);
		// Common sanity checks (number of types, must have Observation type)
		Stream.of(pxProps, tlProps).forEach(p -> {
			assertEquals(2, p.size());
			assertTrue(p.contains(QB.Observation.asNode()));
		});
		// Check that additional custom types are correct
		assertTrue(pxProps.contains(LED.Pixel.asNode()));
		assertTrue(tlProps.contains(LED.GridSquare.asNode()));
	}
}
