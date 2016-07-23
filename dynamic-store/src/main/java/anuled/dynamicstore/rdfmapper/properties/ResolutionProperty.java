package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;

import org.apache.jena.rdf.model.Resource;

import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.util.JenaUtil;
import anuled.vocabulary.LED;

public class ResolutionProperty implements ObservationProperty {
	@Override
	public String getURI() {
		return LED.resolution.getURI();
	}

	@Override
	public Stream<Resource> valuesForObservation(Observation obs) {
		double res = obs.getResolution();
		return Stream.of(JenaUtil.createLiteralResource(res));
	}
}