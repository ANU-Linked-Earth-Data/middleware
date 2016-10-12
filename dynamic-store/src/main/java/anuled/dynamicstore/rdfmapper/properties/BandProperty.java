package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;

import anuled.dynamicstore.Util;
import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.rdfmapper.ObservationFilter;
import anuled.vocabulary.LED;

public class BandProperty implements ObservationProperty {

	@Override
	public String getURI() {
		return LED.etmBand.getURI();
	}

	@Override
	public Stream<Node> valuesForObservation(Observation obs) {
		return Stream.of(Util.createLiteralNode(obs.getBand()));
	}

	@Override
	public void applyToFilter(ObservationFilter filter, Node value) {
		int band;
		try {
			String literalValue = value.getLiteral().getLexicalForm();
			band = Integer.parseInt(literalValue);
		} catch (UnsupportedOperationException|NumberFormatException e) {
			filter.constrainImpossibly();
			return;
		}
		filter.constrainBandNum(band);
	}
}
