package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;

import anuled.dynamicstore.Util;
import anuled.dynamicstore.backend.Observation;
import anuled.vocabulary.LED;

public class BoundsProperty implements ObservationProperty {
	@Override
	public String getURI() {
		return LED.bounds.getURI();
	}

	/** Convert a pixel into a WKT polygon */
	private static String observationToPolyWKT(Observation obs) {
		// Converts [[0, 1], [1, 2], ...] to '0 1, 1 2, ...'
		String innerString = obs.getCell().getBounds().stream()
				.map(p -> p.get(0) + " " + p.get(1))
				.reduce("", (l, r) -> {
					String mid = l.length() > 0 && r.length() > 0 ? ", " : "";
					return l + mid + r;
				});
		return String.format("POLYGON((%s))", innerString);
	}

	@Override
	public Stream<Node> valuesForObservation(Observation obs, String qbDatasetURI) {
		Node rv = Util.createLiteralNode(observationToPolyWKT(obs),
				"http://www.opengis.net/ont/geosparql#wktLiteral");
		return Stream.of(rv);
	}
}
