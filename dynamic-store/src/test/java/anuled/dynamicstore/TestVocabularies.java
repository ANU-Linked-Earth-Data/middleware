package anuled.dynamicstore;

import org.junit.Test;

import anuled.vocabulary.GCMDInstrument;
import anuled.vocabulary.GCMDPlatform;
import anuled.vocabulary.Geo;
import anuled.vocabulary.LED;
import anuled.vocabulary.QB;

import static org.junit.Assert.*;

/**
 * I'm going to be brutally honest here: this class is mostly a hack to drive up
 * code coverage.
 */
public class TestVocabularies {
	@Test
	public void TestVocab() {
		assertTrue(GCMDInstrument.ETM.getURI().startsWith(
				"http://geobrain.laits.gmu.edu/ontology/2004/11/gcmd-instrument.owl#"));
		assertTrue(GCMDPlatform.LANDSAT_7.getURI().startsWith(
				"http://geobrain.laits.gmu.edu/ontology/2004/11/gcmd-platform.owl#"));
		assertTrue(Geo.lat.getURI()
				.startsWith("http://www.w3.org/2003/01/geo/wgs84_pos#"));
		assertTrue(LED.value.getURI()
				.startsWith("http://www.anulinkedearth.org/"));
		assertTrue(QB.ComponentProperty.getURI()
				.startsWith("http://purl.org/linked-data/cube#"));
	}
}
