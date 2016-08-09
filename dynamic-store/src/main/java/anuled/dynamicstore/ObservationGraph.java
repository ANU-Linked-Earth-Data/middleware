package anuled.dynamicstore;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

import anuled.dynamicstore.backend.HDF5Dataset;
import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.rdfmapper.ObservationFilter;
import anuled.dynamicstore.rdfmapper.ObservationMeta;
import anuled.dynamicstore.rdfmapper.URLScheme;
import anuled.dynamicstore.rdfmapper.properties.ObservationProperty;
import anuled.dynamicstore.rdfmapper.properties.PropertyIndex;
import anuled.dynamicstore.util.JenaUtil;

/**
 * Jena graph which can be queried from satellite data, retrieved from an HDF5
 * file. This will get refactored later on (so that multiple tiles are
 * supported, new rasters can be added, etc.)
 */
public final class ObservationGraph extends GraphBase {
	/**
	 * Will store <code>qb:Dataset</code>,
	 * <code>qb:DatastructureDefinition</code>, etc. These properties will be
	 * searched first on any query (we can speed that up later)
	 */
	private HDF5Dataset reader;
	private String qbDataSetURI;

	public ObservationGraph(String h5Filename, String qbDataSetURI) {
		super();
		reader = new HDF5Dataset(h5Filename);
		this.qbDataSetURI = qbDataSetURI;
	}
	
	public ObservationGraph(HDF5Dataset reader, String qbDataSetURI) {
		super();
		this.reader = reader;
		this.qbDataSetURI = qbDataSetURI;
	}

	/**
	 * Make sure that the triple is consistent with the desired object.
	 */
	private static boolean objMatches(Triple trip, Node obj) {
		Object tObj = trip.getObject();
		if (obj == null) {
			// obj == null means "triple's object can be anything you want"
			return true;
		}
		return tObj.equals(obj);
	}

	/**
	 * Retrieve the triples associated with an observation.
	 * 
	 * @param obs
	 *            the observation in question
	 * @param pred
	 *            a node representing the desired predicate (or null, if none
	 *            desired)
	 * @param obj
	 *            a node representing the desired object value (or null, if none
	 *            desired)
	 * @return stream of triples for the observation
	 */
	protected Stream<Triple> mapToTriples(Observation obs, Node pred,
			Node obj) {
		String obsURL = URLScheme.observationURL(obs);
		Node obsNode = JenaUtil.createURINode(obsURL);
		if (pred != null) {
			// We only fetch the matching predicate
			if (pred.isURI()) {
				ObservationProperty prop = PropertyIndex
						.getProperty(pred.getURI());
				Stream<Node> vals = prop.valuesForObservation(obs, qbDataSetURI);
				return vals.map(val -> new Triple(obsNode, pred, val))
						.filter(t -> objMatches(t, obj));
			}
			return Stream.of();
		}

		// Return triples associated with every predicate
		return PropertyIndex.propertyURIs().stream().flatMap(propURI -> {
			Node propNode = JenaUtil.createURINode(propURI);
			// XXX: Something is going wrong on the next line
			ObservationProperty prop = PropertyIndex.getProperty(propURI);
			Stream<Node> propVals = prop.valuesForObservation(obs, qbDataSetURI);
			return propVals
					.map(objNode -> new Triple(obsNode, propNode, objNode));
		}).filter(t -> objMatches(t, obj));
	}

	/**
	 * Get an observation from a URI
	 * 
	 * @param toParse
	 *            URI to parse
	 * @return the corresponding observation, if it exists; otherwise null
	 */
	protected Observation obsForURI(String toParse) {
		ObservationMeta meta;
		try {
			meta = URLScheme.parseObservationURL(toParse);
		} catch (URLScheme.ParseException e) {
			// Invalid URI
			return null;
		}
		// Might be null if it doesn't exist in the dataset
		return ObservationFilter.retrieveFromMeta(meta, reader);
	}

	private Stream<Observation> getAllObservations() {
		return reader.cells(null, null)
				.flatMap(c -> c.observations(null, null));
	}

	/**
	 * Fetch all observations matching a <code>(subj, pred, obj)</code> filter
	 * passed to graphBaseFind().
	 */
	protected Stream<Observation> matchingObservations(Node subj, Node pred,
			Node obj) {
		if (subj != null) {
			// If the subject is a URI, we can try to parse it to figure out
			// which observation it represents; if not, we have nothing to
			// return
			if (subj.isURI()) {
				Observation obs = obsForURI(subj.getURI());
				if (obs != null) {
					return Stream.of(obs);
				}
			}
		} else if (pred != null) {
			if (pred.isURI()) {
				ObservationProperty prop = PropertyIndex
						.getProperty(pred.getURI());
				if (prop != null) {
					if (obj != null) {
						ObservationFilter filter = new ObservationFilter(
								reader);
						filter.constrainProperty(prop.getURI(), obj);
						return filter.execute();
					} else {
						return getAllObservations();
					}
				}
			}
		} else {
			return getAllObservations();
		}
		return Stream.of();
	}

	/**
	 * This is the only method we implement at the moment. The SPARQL layers of
	 * Jena can work with this, but it will be very slow. Ideally we need to
	 * implement an opExecutor or something which does the appropriate
	 * optimisations for our graph.
	 */
	@Override
	protected ExtendedIterator<Triple> graphBaseFind(Triple trip) {
		// For good examples of optimisation opportunities, see:
		// https://www.anutechlauncher.net/projects/linked-earth-observations/wiki/Dynamic_RDF_generation
		Node subj = trip.getMatchSubject(), pred = trip.getMatchPredicate(),
				obj = trip.getMatchObject();
		Stream<Observation> observations = matchingObservations(subj, pred,
				obj);
		Stream<Triple> lsTrips = observations
				.flatMap(obs -> mapToTriples(obs, pred, obj));
		return WrappedIterator.create(lsTrips.iterator());
	}

}