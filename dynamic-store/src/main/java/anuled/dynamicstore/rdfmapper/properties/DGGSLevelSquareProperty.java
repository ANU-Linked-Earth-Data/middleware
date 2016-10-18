package anuled.dynamicstore.rdfmapper.properties;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;

import anuled.dynamicstore.Util;
import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.rdfmapper.ObservationFilter;
import anuled.vocabulary.LED;

public class DGGSLevelSquareProperty implements ObservationProperty {

	@Override
	public String getURI() {
		return LED.dggsLevelSquare.getURI();
	}

	@Override
	public Stream<Node> valuesForObservation(Observation obs, String qbDatasetURI) {
		return Stream.of(Util.createLiteralNode(obs.getCellLevel()));
	}
	
	@Override
	public void applyToFilter(ObservationFilter filter, Node value) {
		Optional<Integer> realVal = Util.toInt(value);
		if (realVal.isPresent()) {
			filter.constrainLevel(realVal.get());
		} else {
			filter.constrainImpossibly();
		}
	}

}
