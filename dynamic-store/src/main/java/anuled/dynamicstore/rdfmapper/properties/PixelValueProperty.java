package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;

import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.backend.PixelObservation;
import anuled.dynamicstore.util.JenaUtil;
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
			return Stream.of(JenaUtil.createLiteralNode(px));
		}
		return Stream.of();
	}

}