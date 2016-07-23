package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.rdfmapper.ObservationFilter;
import anuled.vocabulary.LED;
import anuled.vocabulary.QB;

public class RDFTypeProperty implements ObservationProperty {
	static {
		PropertyIndex.register(new RDFTypeProperty());
	}

	@Override
	public String getURI() {
		return RDF.type.getURI();
	}

	@Override
	public Stream<Resource> valuesForObservation(Observation obs) {
		Builder<Resource> builder = Stream.builder();
		builder.add(QB.Observation);
		if (obs.getPixelLevel() == obs.getCellLevel()) {
			builder.add(LED.Pixel);
		} else {
			builder.add(LED.GridSquare);
		}
		return builder.build();
	}

	@Override
	public void applyToFilter(ObservationFilter filter, Resource value) {
		if (LED.Pixel.equals(value)) {
			filter.constrainToPixel();
		} else if (LED.GridSquare.equals(value)) {
			filter.constrainToTile();
		} else if (!QB.Observation.equals(value)) {
			filter.constrainImpossibly();
		}
	}

}
