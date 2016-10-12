package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;

import anuled.dynamicstore.Util;
import anuled.dynamicstore.backend.Observation;
import anuled.vocabulary.QB;

public class DataSetProperty implements ObservationProperty {

	@Override
	public String getURI() {
		return QB.dataSet.getURI();
	}

	@Override
	public Stream<Node> valuesForObservation(Observation obs) {
		throw new RuntimeException("Need to call valuesForObservation(obs, hld) instead");
	}

	@Override
	public Stream<Node> valuesForObservation(Observation obs, String qbDataSetURI) {
		return Stream.of(Util.createURINode(qbDataSetURI));
	}

}
