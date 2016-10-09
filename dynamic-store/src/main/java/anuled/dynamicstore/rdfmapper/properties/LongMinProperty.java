package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;

import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.util.JenaUtil;
import anuled.vocabulary.LED;

public class LongMinProperty implements ObservationProperty {
	@Override
	public String getURI() {
		return LED.longMin.getURI();
	}

	@Override
	public Stream<Node> valuesForObservation(Observation obs) {
		return Stream.of(JenaUtil.createLiteralNode(obs.getCell().getLongMin()));
	}
}