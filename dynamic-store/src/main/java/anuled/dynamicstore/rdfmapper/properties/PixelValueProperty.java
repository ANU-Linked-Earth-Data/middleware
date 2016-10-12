package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;

import anuled.dynamicstore.Util;
import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.backend.PixelObservation;
import anuled.vocabulary.LED;

public class PixelValueProperty implements ObservationProperty {

	@Override
	public String getURI() {
		return LED.value.getURI();
	}

	@Override
	public Stream<Node> valuesForObservation(Observation obs) {
		if (obs instanceof PixelObservation) {
			PixelObservation pxObs = (PixelObservation) obs;
			double px = pxObs.getPixel();
			return Stream.of(Util.createLiteralNode(px));
		}
		return Stream.of();
	}

}