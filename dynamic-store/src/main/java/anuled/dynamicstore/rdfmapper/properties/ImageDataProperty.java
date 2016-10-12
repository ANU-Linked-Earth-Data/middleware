package anuled.dynamicstore.rdfmapper.properties;

import java.util.Base64;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;

import anuled.dynamicstore.Util;
import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.backend.TileObservation;
import anuled.vocabulary.LED;

public class ImageDataProperty implements ObservationProperty {

	@Override
	public String getURI() {
		return LED.imageData.getURI();
	}

	@Override
	public Stream<Node> valuesForObservation(Observation obs) {
		if (obs instanceof TileObservation){
			TileObservation tileObs = (TileObservation) obs;
			String b64Data = Base64.getEncoder().encodeToString(tileObs.getTile());
			String dataURI = "data:image/png;base64," + b64Data;
			return Stream.of(Util.createURINode(dataURI));
		}
		// For PixelObservations, we can't do anything
		return Stream.of();
	}

}
