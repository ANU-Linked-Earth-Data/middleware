package anuled.dynamicstore;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.stream.Stream;

import org.apache.jena.graph.FrontsTriple;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

import anuled.dynamicstore.backend.Cell;
import anuled.dynamicstore.backend.HDF5Dataset;
import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.backend.PixelObservation;
import anuled.dynamicstore.backend.TileObservation;
import anuled.dynamicstore.rdfmapper.URLScheme;
import anuled.vocabulary.Geo;
import anuled.vocabulary.LED;
import anuled.vocabulary.QB;

/**
 * Jena graph which can be queried from LandSat data, retrieved from a GeoTIFF
 * file. This will get refactored later on (so that multiple tiles are
 * supported, new rasters can be added, etc.)
 */
public final class LandsatGraph extends GraphBase {
	/**
	 * Will store <code>qb:Dataset</code>,
	 * <code>qb:DatastructureDefinition</code>, etc. These properties will be
	 * searched first on any query (we can speed that up later)
	 */
	private Model dataCubeMeta = ModelFactory.createDefaultModel();
	private HDF5Dataset reader;
	private Resource datasetResource;

	public LandsatGraph(String h5Filename) {
		super();
		initMeta();
		reader = new HDF5Dataset(h5Filename);
	}

	/** Initialise metadata associated with this dataset. */
	private void initMeta() {
		// TODO: Make metadata customisable. Should be able to pass in a Turtle
		// file from the command line or something like that.
		InputStream stream = LandsatGraph.class
				.getResourceAsStream("/cube-meta.ttl");
		dataCubeMeta.read(stream, null, "TTL");
		try {
			stream.close();
		} catch (IOException e) {
			throw new RuntimeException("Error closing metadata stream", e);
		}

		// find the URI for the actual dataset
		StmtIterator iter = dataCubeMeta.listStatements(null, RDF.type,
				QB.DataSet);
		if (!iter.hasNext()) {
			throw new RuntimeException(
					"Invalid configuration: need a qb:DataSet");
		}
		datasetResource = iter.nextStatement().getSubject();
		if (iter.hasNext()) {
			throw new RuntimeException(
					"Invalid configuration: can't have more than one qb:DataSet");
		}
	}

	/** Convert a pixel into a WKT polygon */

	private static String observationToPolyWKT(Observation obs) {
		// Converts [[0, 1], [1, 2], ...] to '0 1, 1 2, ...'
		String innerString = obs.getCell().getBounds().stream()
				.map(p -> p.get(0) + " " + p.get(1))
				.reduce("", (l, r) -> l + ", " + r);
		return String.format("POLYGON((%s))", innerString);
	}

	/**
	 * Convert a HDF5Dataset.Observation to a list of triples; will use pixel
	 * data if usePixel is specified, otherwise tile data.
	 */
	private Stream<Triple> observationToTriples(Observation obs) {
		Model pxModel = ModelFactory.createDefaultModel();
		Cell cell = obs.getCell();
		GregorianCalendar obsTimestamp = GregorianCalendar
				.from(cell.getDataset().getTimestamp().toZonedDateTime());

		String url = URLScheme.observationURL(obs);
		Resource res = pxModel.createResource(url)
				.addProperty(RDF.type, QB.Observation)
				.addProperty(QB.dataSet, datasetResource)
				.addLiteral(LED.time, obsTimestamp)
				.addLiteral(LED.resolution, 0.0)
				.addProperty(LED.bounds,
						pxModel.createTypedLiteral(observationToPolyWKT(obs),
								"http://www.opengis.net/ont/geosparql#wktLiteral"))
				.addLiteral(Geo.lat, cell.getLat())
				.addLiteral(Geo.long_, cell.getLon())
				.addProperty(LED.resolution,
						pxModel.createTypedLiteral(obs.getResolution(),
								LED.pixelsPerDegree.getURI()))
				.addLiteral(LED.dggsCell, obs.getCell().getDGGSIdent())
				.addLiteral(LED.dggsLevelSquare, obs.getCellLevel())
				.addLiteral(LED.dggsLevelPixel, obs.getPixelLevel())
				.addLiteral(LED.etmBand, obs.getBand());

		if (obs instanceof PixelObservation) {
			PixelObservation pixelObs = (PixelObservation) obs;
			res.addProperty(RDF.type, LED.Pixel).addProperty(LED.value,
					pxModel.createTypedLiteral(pixelObs.getPixel()));
		} else if (obs instanceof TileObservation) {
			TileObservation tileObs = (TileObservation) obs;
			short invalidValue = tileObs.getCell().getInvalidValue();
			BufferedImage tileImage = Util.arrayToImage(tileObs.getTile(),
					invalidValue);
			String dataURI = Util.imageToPNGURL(tileImage);
			res.addProperty(RDF.type, LED.GridSquare).addProperty(LED.imageData,
					pxModel.createResource(dataURI));
		} else {
			throw new RuntimeException(
					"All observations should be either tiles or pixels, but obs is neither");
		}

		// It would be more efficient to convert a Model to a Stream directly,
		// but I'll leave that for future optimisation
		return pxModel.listStatements().toList().stream()
				.map(FrontsTriple::asTriple);
	}

	/**
	 * Iterate over all pixels in the attached Landsat tile, returning each as
	 * an RDF graph.
	 */
	private Stream<Triple> pixelStream() {
		return reader.cells(null, null).flatMap(c -> c.observations(null, null))
				.flatMap(this::observationToTriples);
	}

	/**
	 * This is the only method we implement at the moment. The SPARQL layers of
	 * Jena can work with this, but it will be very slow. Ideally we need to
	 * implement an opExecutor or something which does the appropriate
	 * optimisations for our graph.
	 */
	@Override
	protected ExtendedIterator<Triple> graphBaseFind(Triple trip) {
		// Get metadata first
		Statement stmt = dataCubeMeta.asStatement(trip);
		StmtIterator metaStmts = dataCubeMeta.listStatements(stmt.getSubject(),
				stmt.getPredicate(), stmt.getObject());
		ExtendedIterator<Triple> rv = metaStmts.mapWith(FrontsTriple::asTriple);

		// For good examples of optimisation opportunities, see:
		// https://www.anutechlauncher.net/projects/linked-earth-observations/wiki/Dynamic_RDF_generation
/*		Node subj = trip.getMatchSubject(), pred = trip.getMatchPredicate(),
				obj = trip.getMatchObject();
		if (subj != null) {
			// If the subject is a URI, we can try to parse it to figure out
			// which observation it represents; if not, we have nothing to
			// return
			if (subj.isURI()) {
				String toParse = subj.getURI();
				// TODO: Need both parser and new object hierarchy to be
				// implemented before I do anything substantial here
			}
		} else if (pred != null && obj != null) {
			// TODO: Can handle predicates like rdf:type here
		} else {
			// TODO
		}*/
		rv = rv.andThen(pixelStream().filter(trip::matches).iterator());
		return rv;
	}

}
