package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;

import org.apache.jena.rdf.model.Resource;

import anuled.dynamicstore.backend.Observation;
import anuled.vocabulary.QB;

public class DataSetProperty implements ObservationProperty {

	@Override
	public String getURI() {
		return QB.dataSet.getURI();
	}

	@Override
	public Stream<Resource> valuesForObservation(Observation obs) {
		// TODO Get the real dataset URI
		return Stream.of();
	}

}
