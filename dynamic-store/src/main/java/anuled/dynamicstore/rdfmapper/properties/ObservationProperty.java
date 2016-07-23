package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;

import org.apache.jena.rdf.model.Resource;

import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.rdfmapper.ObservationFilter;

public interface ObservationProperty {
	public String getURI();
	public Stream<Resource> valuesForObservation(Observation obs);
	public default void applyToFilter(ObservationFilter filter, Resource value) {
		filter.constrainNaively(this, value);
	}
}
