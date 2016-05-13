package anuled.dynamicstore;

import java.util.Iterator;
import java.util.function.Function;

import org.apache.jena.graph.FrontsTriple;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import com.google.common.collect.Iterables;

import anuled.vocabulary.GCMDInstrument;
import anuled.vocabulary.GCMDPlatform;
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
	private Resource qbStructure, qbDSDefinition;
	private final String prefix = "http://www.example.org/ANU-LED-example#";
	private TileReader reader;
	private Resource timeAP;

	public LandsatGraph(String tileFilename) {
		super();
		initMeta();
		reader = new TileReader(tileFilename);
	}

	/** Initialise metadata associated with this dataset. */
	private void initMeta() {
		// TODO: Make these dynamic/customisable. Should probably just use a
		// Turtle file for metadata
		Resource instrumentAP = addAttributeProperty("instrument",
				GCMDInstrument.Instrument);
		Resource satelliteAP = addAttributeProperty("satellite",
				GCMDPlatform.PLATFORM);
		timeAP = addAttributeProperty("time", XSD.dateTimeStamp);
		Resource bandAP = addAttributeProperty("band", XSD.integer);

		Function<Resource, Property> asProp = (Resource res) -> dataCubeMeta
				.createProperty(res.getURI());
		qbStructure = dataCubeMeta
				.createResource(prefix + "landsatDataStructure")
				.addProperty(RDF.type, QB.DataStructureDefinition)
				// Jena has a really weird Property/Resource split. They're both
				// URIs, so why can't I mix and match them?
				.addProperty(asProp.apply(instrumentAP), GCMDInstrument.ETM)
				.addProperty(asProp.apply(satelliteAP), GCMDPlatform.LANDSAT)
				.addProperty(asProp.apply(bandAP),
						dataCubeMeta.createTypedLiteral(4));
		qbDSDefinition = dataCubeMeta.createResource(prefix + "landsatData")
				.addProperty(RDF.type, QB.DataSet)
				.addProperty(QB.structure, qbStructure);

		addCSDimension("positionComponent", LED.location);
		addCSDimension("timeComponent", timeAP);
		addCSDimension("dataComponent", LED.imageData);

		addCSAttribute("instrumentComponent", instrumentAP);
		addCSAttribute("satelliteComponent", satelliteAP);
		addCSAttribute("bandComponent", bandAP);
	}

	private Resource addAttributeProperty(String name, Resource range) {
		return dataCubeMeta.createResource(prefix + name)
				.addProperty(RDFS.range, range);
	}

	private Resource addCSDimension(String name, Resource dimension) {
		Resource rv = dataCubeMeta.createResource(prefix + name)
				.addProperty(QB.dimension, dimension)
				.addProperty(RDF.type, QB.ComponentSpecification);
		qbDSDefinition.addProperty(QB.component, rv);
		return rv;
	}

	private Resource addCSAttribute(String name, Resource attribute) {
		Resource rv = dataCubeMeta.createResource(prefix + name)
				.addProperty(QB.attribute, attribute)
				.addProperty(RDF.type, QB.ComponentSpecification);
		qbDSDefinition.addProperty(QB.component, rv);
		return rv;
	}

	/** Convert a pixel into a WKT polygon */
	private String pixelToPolyWKT(TileReader.Pixel p) {
		double top = p.latlong[0] + reader.getPixelHeight() / 2;
		double bot = p.latlong[0] - reader.getPixelHeight() / 2;
		double left = p.latlong[1] - reader.getPixelWidth() / 2;
		double right = p.latlong[1] + reader.getPixelWidth() / 2;
		return String.format("POLYGON((%f %f, %f %f, %f %f, %f %f, %f %f))",
				top, left, top, right, bot, right, bot, left, top, left);
	}

	private Iterable<Triple> pixelToTriples(TileReader.Pixel p) {
		Model pxModel = ModelFactory.createDefaultModel();

		pxModel.createResource(prefix + "/pixel-" + p.row + "-" + p.col)
				.addProperty(RDF.type, LED.Pixel)
				.addProperty(RDF.type, QB.Observation)
				// XXX: Using p.pixel[0] is badly broken becuase (a) it might
				// be out of bounds and (b) it will ignore the rest of the bands
				.addProperty(LED.imageData,
						pxModel.createTypedLiteral(p.pixel[0]))
				// TODO: This should be an xsd:dateTime (so pass
				// .createTypedLiteral a Java Calendar object)
				.addProperty(pxModel.createProperty(timeAP.getURI()),
						pxModel.createLiteral(""))
				.addProperty(LED.resolution, pxModel.createTypedLiteral(0.0))
				.addProperty(LED.bounds,
						pxModel.createTypedLiteral(pixelToPolyWKT(p),
								"http://www.opengis.net/ont/geosparql#wktLiteral"))
				.addProperty(LED.location, pxModel.createResource()
						.addProperty(Geo.lat,
								pxModel.createTypedLiteral(p.latlong[0]))
						.addProperty(Geo.long_,
								pxModel.createTypedLiteral(p.latlong[1])));

		// Return an iterable which runs over all the triples in the model we
		// created above
		return new Iterable<Triple>() {
			public Iterator<Triple> iterator() {
				return pxModel.listStatements().mapWith(FrontsTriple::asTriple);
			};
		};
	}

	/**
	 * Iterate over all pixels in the attached Landsat tile, returning each as
	 * an RDF graph.
	 */
	private Iterable<Triple> pixelIterable() {
		return Iterables.concat(
				Iterables.transform(reader.pixels(), this::pixelToTriples));
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
		rv = rv.andThen(pixelIterable().iterator());
		return rv;
	}

}
