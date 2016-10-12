package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;

import anuled.dynamicstore.Util;
import anuled.dynamicstore.backend.Observation;
import anuled.vocabulary.LED;

public class ResolutionProperty implements ObservationProperty {
	@Override
	public String getURI() {
		return LED.resolution.getURI();
	}

	@Override
	public Stream<Node> valuesForObservation(Observation obs) {
		double res = obs.getResolution();
		return Stream.of(Util.createLiteralNode(res));
	}
}