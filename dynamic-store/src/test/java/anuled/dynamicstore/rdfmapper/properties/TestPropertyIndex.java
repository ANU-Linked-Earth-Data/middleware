package anuled.dynamicstore.rdfmapper.properties;

import static org.junit.Assert.*;

import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

import anuled.vocabulary.LED;
import anuled.dynamicstore.rdfmapper.properties.*;

@SuppressWarnings("unused")
public class TestPropertyIndex {

	@Test
	public void testGetProperty() {
		ObservationProperty prop = PropertyIndex.getProperty(LED.dggsCell.getURI());
		assertNotNull(prop);
		assertTrue(prop instanceof DGGSCellProperty);
		
		prop = PropertyIndex.getProperty(RDF.type.getURI());
		assertNotNull(prop);
		assertTrue(prop instanceof RDFTypeProperty);
		
		prop = PropertyIndex.getProperty(LED.GridSquare.getURI());
		assertNull(prop);
	}
	
	@Test
	public void testPropertyStream() {
		Set<String> allProps = PropertyIndex.propertyURIs();
		assertEquals(13, allProps.size());
		assertTrue(allProps.contains(LED.etmBand.getURI()));
	}

}
