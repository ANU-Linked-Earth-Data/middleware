package anuled.dynamicstore.rdfmapper.properties;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;

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
	public Stream<Node> valuesForObservation(Observation obs) {
		return Stream.of(
				JenaUtil.createLiteralNode(obs.getCell().getDGGSIdent()));
	}
	
	@Override
	public void applyToFilter(ObservationFilter filter, Node value) {
		String cellID;
		try {
			cellID = value.getLiteral().getLexicalForm();
		} catch (UnsupportedOperationException e) {
			filter.constrainImpossibly();
			return;
		}
		filter.constrainCellID(cellID);
	}

}
