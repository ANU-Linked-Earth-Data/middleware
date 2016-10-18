package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;

import anuled.dynamicstore.Util;
import anuled.dynamicstore.backend.Observation;
import anuled.vocabulary.LED;

public class LatMaxProperty implements ObservationProperty {
	@Override
	public String getURI() {
		return LED.latMax.getURI();
	}

	@Override
	public Stream<Node> valuesForObservation(Observation obs, String qbDatasetURI) {
		return Stream.of(Util.createLiteralNode(obs.getCell().getLatMax()));
	}
}
