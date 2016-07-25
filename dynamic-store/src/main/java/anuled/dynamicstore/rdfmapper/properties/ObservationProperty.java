package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;

import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.rdfmapper.ObservationFilter;

public interface ObservationProperty {
	public String getURI();
	public Stream<Node> valuesForObservation(Observation obs);
	public default void applyToFilter(ObservationFilter filter, Node value) {
		filter.constrainNaively(this, value);
	}
}
