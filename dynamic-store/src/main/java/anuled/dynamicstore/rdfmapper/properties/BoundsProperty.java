package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;

import org.apache.jena.rdf.model.Resource;

import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.util.JenaUtil;
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
				.reduce("", (l, r) -> l + ", " + r);
		return String.format("POLYGON((%s))", innerString);
	}

	@Override
	public Stream<Resource> valuesForObservation(Observation obs) {
		Resource rv = JenaUtil.createLiteralResource(observationToPolyWKT(obs),
				"http://www.opengis.net/ont/geosparql#wktLiteral");
		return Stream.of(rv);
	}
}
