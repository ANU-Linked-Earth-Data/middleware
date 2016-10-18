package anuled.dynamicstore.rdfmapper.properties;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;

import anuled.dynamicstore.Util;
import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.rdfmapper.ObservationFilter;
import anuled.vocabulary.LED;

public class BandProperty implements ObservationProperty {

	@Override
	public String getURI() {
		return LED.etmBand.getURI();
	}

	@Override
	public Stream<Node> valuesForObservation(Observation obs, String qbDatasetURI) {
		return Stream.of(Util.createLiteralNode(obs.getBand()));
	}

	@Override
	public void applyToFilter(ObservationFilter filter, Node value) {
		Optional<Integer> bandNum = Util.toInt(value);
		if (bandNum.isPresent()) {
			filter.constrainBandNum(bandNum.get());
		} else {
			filter.constrainImpossibly();
		}
	}
}
