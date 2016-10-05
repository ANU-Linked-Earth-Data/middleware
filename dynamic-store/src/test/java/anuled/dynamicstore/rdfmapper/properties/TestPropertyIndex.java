package anuled.dynamicstore.rdfmapper.properties;

import static org.junit.Assert.*;

import java.util.Set;

import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

import anuled.vocabulary.LED;
import anuled.dynamicstore.rdfmapper.properties.*;
import anuled.dynamicstore.rdfmapper.properties.LatLonBoxProperty.BoundType;

@SuppressWarnings("unused")
public class TestPropertyIndex {
	@Test
	public void testGetProperty() {
		ObservationProperty prop = PropertyIndex
				.getProperty(LED.dggsCell.getURI()).get();
		assertNotNull(prop);
		assertTrue(prop instanceof DGGSCellProperty);

		prop = PropertyIndex.getProperty(RDF.type.getURI()).get();
		assertNotNull(prop);
		assertTrue(prop instanceof RDFTypeProperty);

		assertFalse(
				PropertyIndex.getProperty(LED.GridSquare.getURI()).isPresent());

		// LatLonBoxProperty instances are weird. Make sure they're all in the
		// index, but not externally visible!
		for (BoundType type : BoundType.values()) {
			String uri = type.getURI();
			assertTrue(PropertyIndex.getProperty(uri).isPresent());
			assertFalse(PropertyIndex.externalPropertyURIs().contains(uri));
		}
	}

	@Test
	public void testPropertyStream() {
		Set<String> allProps = PropertyIndex.externalPropertyURIs();
		assertEquals(17, allProps.size());
		assertTrue(allProps.contains(LED.etmBand.getURI()));
	}

}
