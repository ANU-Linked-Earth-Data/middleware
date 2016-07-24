package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.rdf.model.LiteralRequiredException;
import org.apache.jena.rdf.model.Resource;

import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.rdfmapper.ObservationFilter;
import anuled.dynamicstore.util.JenaUtil;
import anuled.vocabulary.LED;

public class DGGSCellProperty implements ObservationProperty {

	@Override
	public String getURI() {
		return LED.dggsCell.getURI();
	}

	@Override
	public Stream<Resource> valuesForObservation(Observation obs) {
		return Stream.of(
				JenaUtil.createLiteralResource(obs.getCell().getDGGSIdent()));
	}
	
	@Override
	public void applyToFilter(ObservationFilter filter, Resource value) {
		String cellID;
		try {
			cellID = value.asLiteral().getString();
		} catch (DatatypeFormatException|LiteralRequiredException e) {
			filter.constrainImpossibly();
			return;
		}
		filter.constrainCellID(cellID);
	}

}
