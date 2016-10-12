package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;

import anuled.dynamicstore.Util;
import anuled.dynamicstore.backend.Observation;
import anuled.vocabulary.Geo;

public class LatProperty implements ObservationProperty {
	@Override
	public String getURI() {
		return Geo.lat.getURI();
	}

	@Override
	public Stream<Node> valuesForObservation(Observation obs) {
		return Stream.of(Util.createLiteralNode(obs.getCell().getLat()));
	}
}
