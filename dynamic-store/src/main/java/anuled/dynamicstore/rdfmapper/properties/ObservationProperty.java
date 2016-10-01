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

	/**
	 * Some URIs are internal to the middleware (e.g.
	 * <code>LatLonBoxProperty</code>). We don't want those used to generate
	 * bindings which are sent back to the user (for example), so we mark them
	 * as internal.
	 */
	public default boolean isInternal() {
		return false;
	}
}
