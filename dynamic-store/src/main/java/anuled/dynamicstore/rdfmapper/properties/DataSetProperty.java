package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;

import anuled.dynamicstore.backend.Observation;
import anuled.vocabulary.LED;
import anuled.vocabulary.QB;

public class DataSetProperty implements ObservationProperty {

	@Override
	public String getURI() {
		return QB.dataSet.getURI();
	}

	@Override
	public Stream<Node> valuesForObservation(Observation obs) {
		// TODO Get the real dataset URI
		return Stream.of(LED.ObservationGraph.asNode());
	}

}
