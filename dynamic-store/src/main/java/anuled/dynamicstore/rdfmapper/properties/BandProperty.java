package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.rdf.model.LiteralRequiredException;
import org.apache.jena.rdf.model.Resource;
import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.rdfmapper.ObservationFilter;
import anuled.dynamicstore.util.JenaUtil;
import anuled.vocabulary.QB;

public class BandProperty implements ObservationProperty {

	@Override
	public String getURI() {
		return QB.Observation.getURI();
	}

	@Override
	public Stream<Resource> valuesForObservation(Observation obs) {
		return Stream.of(JenaUtil.createLiteralResource(obs.getBand()));
	}

	@Override
	public void applyToFilter(ObservationFilter filter, Resource value) {
		try {
			int band = value.asLiteral().getInt();
			filter.constrainBandNum(band);
		} catch (DatatypeFormatException|LiteralRequiredException e) {
			filter.constrainImpossibly();
		}
	}
}
