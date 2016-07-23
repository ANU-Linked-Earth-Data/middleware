package anuled.dynamicstore.rdfmapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import anuled.dynamicstore.backend.Cell;
import anuled.dynamicstore.backend.HDF5Dataset;
import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.backend.PixelObservation;
import anuled.dynamicstore.backend.TileObservation;
import anuled.dynamicstore.rdfmapper.properties.ObservationProperty;
import anuled.dynamicstore.rdfmapper.properties.PropertyIndex;

/**
 * Class for filtering observations in the virtual RDF graph.
 */
public class ObservationFilter {
	// Things we can filter the underlying observations by: whether they're
	// pixels or tiles, band number, DGGS cell, level of DGGS cell in hierarchy
	String reqCellID = null;
	Integer reqBandNum = null;
	Integer reqLevel = null;
	Class<?> reqClass = null;
	// set empty = true when there are no matching observations
	boolean empty = false;
	List<Pair<ObservationProperty, Resource>> naiveConstraints = new ArrayList<>();
	HDF5Dataset dataset;

	public ObservationFilter(HDF5Dataset dataset) {
		this.dataset = dataset;
	}

	public void constrainProperty(Property property, Resource expectedValue) {
		ObservationProperty prop = PropertyIndex.getProperty(property.getURI());
		if (prop == null) {
			constrainImpossibly();
		} else {
			prop.applyToFilter(this, expectedValue);
		}
	}

	public void constrainCellID(String newCellID) {
		if (reqCellID == null || reqCellID.equals(newCellID)) {
			reqCellID = newCellID;
		} else {
			// can't have two cell IDs
			constrainImpossibly();
		}
	}

	public void constrainBandNum(int bandNum) {
		if (reqBandNum == null || reqBandNum.equals(bandNum)) {
			reqBandNum = bandNum;
		} else {
			constrainImpossibly();
		}
	}

	public void constrainLevel(int level) {
		if (reqLevel == null || reqLevel.equals(level)) {
			reqLevel = level;
		} else {
			constrainImpossibly();
		}
	}

	private void constrainType(Class<?> newClass) {
		if (reqClass == null || reqClass == newClass) {
			reqClass = newClass;
		} else {
			constrainImpossibly();
		}
	}

	public void constrainToPixel() {
		constrainType(PixelObservation.class);
	}

	public void constrainToTile() {
		constrainType(TileObservation.class);
	}

	public void constrainImpossibly() {
		empty = true;
	}

	public Stream<Observation> execute() {
		if (empty) {
			return Stream.of();
		} else {
			Stream<Cell> cells = dataset.cells(reqLevel, reqCellID);
			Stream<Observation> observations = cells
					.flatMap(c -> c.observations(reqBandNum, reqClass))
					.filter(o -> {
						for (Pair<ObservationProperty, Resource> pair : naiveConstraints) {
							ObservationProperty prop = pair.getLeft();
							Resource expected = pair.getRight();
							Stream<Resource> actual = prop
									.valuesForObservation(o);
							if (!actual.anyMatch(v -> expected.equals(v))) {
								return false;
							}
						}
						return true;
					});
			return observations;
		}
	}

	public void constrainNaively(ObservationProperty prop, Resource value) {
		// Optimisation opportunity: check for conflicting values (assuming that
		// the constraints are AND rather than OR)
		naiveConstraints.add(Pair.of(prop, value));
	}
}
