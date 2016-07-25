package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDF;

import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.rdfmapper.ObservationFilter;
import anuled.vocabulary.LED;
import anuled.vocabulary.QB;

public class RDFTypeProperty implements ObservationProperty {
	@Override
	public String getURI() {
		return RDF.type.getURI();
	}

	@Override
	public Stream<Node> valuesForObservation(Observation obs) {
		Builder<Node> builder = Stream.builder();
		builder.add(QB.Observation.asNode());
		if (obs.getPixelLevel() == obs.getCellLevel()) {
			builder.add(LED.Pixel.asNode());
		} else {
			builder.add(LED.GridSquare.asNode());
		}
		return builder.build();
	}

	@Override
	public void applyToFilter(ObservationFilter filter, Node value) {
		if (LED.Pixel.asNode().equals(value)) {
			filter.constrainToPixel();
		} else if (LED.GridSquare.asNode().equals(value)) {
			filter.constrainToTile();
		} else if (!QB.Observation.asNode().equals(value)) {
			filter.constrainImpossibly();
		}
	}

}
