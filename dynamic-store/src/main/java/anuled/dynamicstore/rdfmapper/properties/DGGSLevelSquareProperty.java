package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.rdf.model.LiteralRequiredException;
import org.apache.jena.rdf.model.Resource;

import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.rdfmapper.ObservationFilter;
import anuled.dynamicstore.util.JenaUtil;
import anuled.vocabulary.LED;

public class DGGSLevelSquareProperty implements ObservationProperty {

	@Override
	public String getURI() {
		return LED.dggsLevelSquare.getURI();
	}

	@Override
	public Stream<Resource> valuesForObservation(Observation obs) {
		return Stream.of(JenaUtil.createLiteralResource(obs.getCellLevel()));
	}
	
	@Override
	public void applyToFilter(ObservationFilter filter, Resource value) {
		int res;
		try {
			res = value.asLiteral().getInt();
		} catch (DatatypeFormatException|LiteralRequiredException e) {
			filter.constrainImpossibly();
			return;
		}
		filter.constrainLevel(res);
	}

}
