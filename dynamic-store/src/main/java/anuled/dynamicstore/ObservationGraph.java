package anuled.dynamicstore;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

import anuled.dynamicstore.backend.HDF5Dataset;
import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.backend.Product;
import anuled.dynamicstore.rdfmapper.MetadataFactory;
import anuled.dynamicstore.rdfmapper.ObservationFilter;
import anuled.dynamicstore.rdfmapper.ObservationMeta;
import anuled.dynamicstore.rdfmapper.URLScheme;
import anuled.dynamicstore.rdfmapper.properties.ObservationProperty;
import anuled.dynamicstore.rdfmapper.properties.PropertyIndex;

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
	private String qbDataSetPrefix;

	public ObservationGraph(String h5Filename, String qbDataSetPrefix) {
		super();
		reader = new HDF5Dataset(h5Filename);
		this.qbDataSetPrefix = qbDataSetPrefix;
	}

	public ObservationGraph(HDF5Dataset reader, String qbDataSetPrefix) {
		super();
		this.reader = reader;
		this.qbDataSetPrefix = qbDataSetPrefix;
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
	
	/** Get the qb:Dataset URI for a given product */
	private String datasetFor(Product prod) {
		return MetadataFactory.datasetURI(qbDataSetPrefix, prod);
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
		Node obsNode = new ObservationNode(obs);

		if (pred != null) {
			// We only fetch the matching predicate, if we can
			if (pred.isURI()) {
				Optional<ObservationProperty> maybeProp = PropertyIndex
						.getProperty(pred.getURI());
				if (maybeProp.isPresent()) {
					Stream<Node> vals = maybeProp.get()
							.valuesForObservation(obs, datasetFor(obs.getProduct()));
					return vals.map(val -> new Triple(obsNode, pred, val))
							.filter(t -> objMatches(t, obj));
				}
			}
			return Stream.of();
		}

		// Return triples associated with every predicate
		return PropertyIndex.externalPropertyURIs().stream().flatMap(propURI -> {
			Node propNode = Util.createURINode(propURI);
			ObservationProperty prop = PropertyIndex.getProperty(propURI).get();
			Stream<Node> propVals = prop.valuesForObservation(obs,
					datasetFor(obs.getProduct()));
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
		// So much null… I may be doing this wrong.
		return reader.cells(null, null, null, null, null, null)
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
				Observation obs;
				if (subj instanceof ObservationNode) {
					// Sometimes the previous stage stores the Observation with
					// the node so that we don't have to look it up each time.
					obs = ((ObservationNode) subj).getObservation();
				} else {
					obs = obsForURI(subj.getURI());
				}
				if (obs != null) {
					return Stream.of(obs);
				}
			}
		} else if (pred != null) {
			if (pred.isURI()) {
				Optional<ObservationProperty> maybeProp = PropertyIndex
						.getProperty(pred.getURI());
				if (maybeProp.isPresent()) {
					ObservationProperty prop = maybeProp.get();
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

	public Stream<ObservationNode> observationURIs(List<Triple> pattern) {
		// Get the observation URIs matching the given pattern
		// All subjects in the pattern must be non-concrete; all
		// predicates and objects must be concrete.
		ObservationFilter filter = new ObservationFilter(reader);

		for (Triple trip : pattern) {
			String predURI = trip.getPredicate().getURI();
			assert !trip.getSubject().isConcrete();
			assert trip.getPredicate().isConcrete();
			assert trip.getObject().isConcrete();
			filter.constrainProperty(predURI, trip.getObject());
		}

		return filter.execute().map(obs -> new ObservationNode(obs));
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
