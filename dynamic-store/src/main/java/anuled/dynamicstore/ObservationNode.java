package anuled.dynamicstore;

import org.apache.jena.graph.Node_URI;

import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.rdfmapper.URLScheme;

/**
 * This lets us attach actual Observation pointers to observation URLs.
 * Particularly useful for stuff like GraphBaseFind, since it saves us from
 * parsing a URI into an observation.
 */
public class ObservationNode extends Node_URI {
	private Observation observation;

	public ObservationNode(Observation observation) {
		super(URLScheme.observationURL(observation));
		this.observation = observation;
	}

	public Observation getObservation() {
		return observation;
	}
}
