package anuled.dynamicstore;

import java.util.function.Function;

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

import anuled.vocabulary.GCMDInstrument;
import anuled.vocabulary.GCMDPlatform;
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

	public LandsatGraph() {
		super();
		// TODO: Make these dynamic/customisable. Should probably just use a
		// Turtle file for metadata
		Resource instrumentAP = addAttributeProperty("instrument",
				GCMDInstrument.Instrument);
		Resource satelliteAP = addAttributeProperty("satellite",
				GCMDPlatform.PLATFORM);
		// Not sure what :time is for
		Resource timeAP = addAttributeProperty("time", XSD.dateTimeStamp);
		Resource bandAP = addAttributeProperty("band", XSD.integer);

		Function<Resource, Property> asProp = (Resource res) -> dataCubeMeta.createProperty(res.getURI());
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

	@Override
	protected ExtendedIterator<Triple> graphBaseFind(Triple trip) {
		StmtIterator metaStmts = dataCubeMeta.listStatements();
		// TODO: Concatenate with iterator over triples from data itself
		return metaStmts.mapWith(stmt -> stmt.asTriple());
	}

}
