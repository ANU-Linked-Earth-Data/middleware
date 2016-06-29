package anuled.dynamicstore;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.apache.jena.graph.FrontsTriple;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

import anuled.dynamicstore.HDF5Dataset.Observation;
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
/*	private Resource qbStructure, qbDSDefinition;
	private final String prefix = "http://www.example.org/ANU-LED-example#";
	private Resource timeAP;*/

	public LandsatGraph(String h5Filename) {
		super();
		initMeta();
		reader = new HDF5Dataset(h5Filename);
	}

	/** Initialise metadata associated with this dataset. */
	private void initMeta() {
		// TODO: Make metadata customisable. Should be able to pass in a Turtle
		// file from the command line or something like that.
		InputStream stream = LandsatGraph.class.getResourceAsStream("/cube-meta.ttl");
		dataCubeMeta.read(stream, null, "TTL");
		try {
			stream.close();
		} catch (IOException e) {
			throw new RuntimeException("Error closing metadata stream", e);
		}
	}
	/** Convert a pixel into a WKT polygon */
	/*
	 * private String pixelToPolyWKT(TileReader.Pixel p) { double top =
	 * p.latlong[0] + reader.getPixelHeight() / 2; double bot = p.latlong[0] -
	 * reader.getPixelHeight() / 2; double left = p.latlong[1] -
	 * reader.getPixelWidth() / 2; double right = p.latlong[1] +
	 * reader.getPixelWidth() / 2; return
	 * String.format("POLYGON((%f %f, %f %f, %f %f, %f %f, %f %f))", top, left,
	 * top, right, bot, right, bot, left, top, left); }
	 */

	/*
	 * private Iterable<Triple> pixelToTriples(TileReader.Pixel p) {
	 * 
	 * }
	 */

	/**
	 * Convert a HDF5Dataset.Observation to a list of triples; will use pixel
	 * data if usePixel is specified, otherwise tile data.
	 */
	private Stream<Triple> observationToTriples(Observation obs) {
		Model pxModel = ModelFactory.createDefaultModel();
		HDF5Dataset.Cell cell = obs.getCell();

		String url = URLScheme.observationURL(obs);
		pxModel.createResource(url).addProperty(RDF.type, LED.Pixel)
				.addProperty(RDF.type, QB.Observation)
				// XXX: Using p.pixel[0] is badly broken becuase (a) it might
				// be out of bounds and (b) it will ignore the rest of the bands
				// .addProperty(LED.imageData,
				// pxModel.createTypedLiteral(p.pixel[0]))
				// TODO: This should be an xsd:dateTime (so pass
				// .createTypedLiteral a Java Calendar object)
//				.addProperty(pxModel.createProperty(timeAP.getURI()),
//						pxModel.createLiteral(""))
				.addProperty(LED.resolution, pxModel.createTypedLiteral(0.0))
				// .addProperty(LED.bounds,
				// pxModel.createTypedLiteral(pixelToPolyWKT(p),
				// "http://www.opengis.net/ont/geosparql#wktLiteral"))
				.addProperty(LED.location, pxModel.createResource()
						.addProperty(Geo.lat,
								pxModel.createTypedLiteral(cell.getLat()))
						.addProperty(Geo.long_,
								pxModel.createTypedLiteral(cell.getLon())));

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
		// TODO: I was using Guava here, but it's probably better to use Java 8
		// streams instead.
		return reader.cells().flatMap(c -> c.observations())
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
		StmtIterator metaStmts = dataCubeMeta.listStatements();
		ExtendedIterator<Triple> rv = metaStmts.mapWith(FrontsTriple::asTriple);
		rv = rv.andThen(pixelStream().iterator());
		return rv;
	}

}
