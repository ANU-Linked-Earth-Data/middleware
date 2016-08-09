package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;

import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.rdfmapper.ObservationFilter;

public interface ObservationProperty {
	public String getURI();

	public Stream<Node> valuesForObservation(Observation obs);

	// yeah, this is hacky; I really should have the default the other way
	// around, or maybe nest interfaces :/
	public default Stream<Node> valuesForObservation(Observation obs,
			String qbDataSetURI) {
		return valuesForObservation(obs);
	}

	public default void applyToFilter(ObservationFilter filter, Node value) {
		filter.constrainNaively(this, value);
	}
}
