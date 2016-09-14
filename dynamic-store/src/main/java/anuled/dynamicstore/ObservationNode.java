package anuled.dynamicstore;

import org.apache.jena.graph.Node_URI;

import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.rdfmapper.URLScheme;

/**
 * This lets us attach actual Observation pointers to observation URLs.
 * Particularly useful for stuff like GraphBaseFind, since it saves us from
 * parsing a URI into an observation.
 * 
 * XXX: This is somewhat of a micro-optimisation. Not sure whether we'll
 * actually benefit from it in the long run. Might want to benchmark before
 * throwing it in.
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
