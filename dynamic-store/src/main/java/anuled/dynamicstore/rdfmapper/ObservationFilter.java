package anuled.dynamicstore.rdfmapper;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.graph.Node;

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
	Double latMin = null;
	Double latMax = null;
	Double lonMin = null;
	Double lonMax = null;
	Class<?> reqClass = null;
	// set empty = true when there are no matching observations
	boolean empty = false;
	List<Pair<ObservationProperty, Node>> naiveConstraints = new ArrayList<>();
	HDF5Dataset dataset;
	String qbDatasetPrefix;

	public ObservationFilter(HDF5Dataset dataset, String qbDatasetPrefix) {
		this.dataset = dataset;
		this.qbDatasetPrefix = qbDatasetPrefix;
	}

	public ObservationFilter constrainProperty(String propURI,
			Node expectedValue) {
		// Expected value must be URL/literal/blank
		assert expectedValue.isConcrete();
		Optional<ObservationProperty> maybeProp = PropertyIndex
				.getProperty(propURI);
		if (!maybeProp.isPresent()) {
			constrainImpossibly();
		} else {
			maybeProp.get().applyToFilter(this, expectedValue);
		}
		return this;
	}

	public ObservationFilter constrainCellID(String newCellID) {
		if (reqCellID == null || reqCellID.equals(newCellID)) {
			reqCellID = newCellID;
		} else {
			// can't have two cell IDs
			constrainImpossibly();
		}
		return this;
	}

	public ObservationFilter constrainBandNum(int bandNum) {
		if (reqBandNum == null || reqBandNum.equals(bandNum)) {
			reqBandNum = bandNum;
		} else {
			constrainImpossibly();
		}
		return this;
	}

	public ObservationFilter constrainLevel(int level) {
		if (reqLevel == null || reqLevel.equals(level)) {
			reqLevel = level;
		} else {
			constrainImpossibly();
		}
		return this;
	}

	private ObservationFilter constrainType(Class<?> newClass) {
		if (reqClass == null || reqClass == newClass) {
			reqClass = newClass;
		} else {
			constrainImpossibly();
		}
		return this;
	}

	public ObservationFilter constrainLatMax(double latMax) {
		this.latMax = latMax;
		return this;
	}

	public ObservationFilter constrainLatMin(double latMin) {
		this.latMin = latMin;
		return this;
	}

	public ObservationFilter constrainLonMax(double lonMax) {
		this.lonMax = lonMax;
		return this;
	}

	public ObservationFilter constrainLonMin(double lonMin) {
		this.lonMin = lonMin;
		return this;
	}

	public ObservationFilter constrainToPixel() {
		constrainType(PixelObservation.class);
		return this;
	}

	public ObservationFilter constrainToTile() {
		constrainType(TileObservation.class);
		return this;
	}

	public ObservationFilter constrainNaively(ObservationProperty prop,
			Node value) {
		// Optimisation opportunity: check for conflicting values (assuming that
		// the constraints are AND rather than OR)
		naiveConstraints.add(Pair.of(prop, value));
		return this;
	}

	public ObservationFilter constrainImpossibly() {
		empty = true;
		return this;
	}

	public Stream<Observation> execute() {
		if (empty) {
			return Stream.of();
		} else {
			Stream<Cell> cells = dataset.cells(reqLevel, reqCellID, lonMin,
					lonMax, latMin, latMax);
			Stream<Observation> observations = cells
					.flatMap(c -> c.observations(reqBandNum, reqClass))
					.filter(o -> {
						String qbDatasetURI = MetadataFactory
								.datasetURI(qbDatasetPrefix, o.getProduct());
						for (Pair<ObservationProperty, Node> pair : naiveConstraints) {
							ObservationProperty prop = pair.getLeft();
							Node expected = pair.getRight();
							Stream<Node> actual = prop.valuesForObservation(o,
									qbDatasetURI);
							if (!actual.anyMatch(v -> expected.equals(v))) {
								return false;
							}
						}
						return true;
					});
			return observations;
		}
	}

	public static Observation retrieveFromMeta(ObservationMeta meta,
			HDF5Dataset dataset, String qbDatasetPrefix) {
		ObservationFilter filter = new ObservationFilter(dataset,
				qbDatasetPrefix);
		filter.constrainBandNum(meta.band);
		filter.constrainCellID(meta.cell);
		filter.constrainLevel(meta.levelSquare);
		if (meta.levelPixel == meta.levelSquare) {
			filter.constrainToPixel();
		} else {
			filter.constrainToTile();
		}
		Stream<Observation> results = filter.execute();
		Optional<Observation> maybeFst = results.findFirst();
		Observation fst;
		try {
			fst = maybeFst.get();
		} catch (NoSuchElementException e) {
			return null;
		}

		// We know that band number, cell ID and levelSquare match. Just need to
		// check that levelPixel and datetime match
		boolean tsEqual = fst.getTimestamp().equals(meta.timestamp);
		if (fst.getPixelLevel() == meta.levelPixel && tsEqual) {
			return fst;
		}
		return null;
	}
}
