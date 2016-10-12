package anuled.dynamicstore.rdfmapper.properties;

import java.util.GregorianCalendar;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;

import anuled.dynamicstore.Util;
import anuled.dynamicstore.backend.Observation;
import anuled.vocabulary.LED;

public class TimeProperty implements ObservationProperty {

	@Override
	public String getURI() {
		return LED.time.getURI();
	}

	private static GregorianCalendar getTimestamp(Observation obs) {
		return GregorianCalendar.from(obs.getTimestamp());
	}

	@Override
	public Stream<Node> valuesForObservation(Observation obs) {
		return Stream.of(Util.createLiteralNode(getTimestamp(obs)));
	}
}
