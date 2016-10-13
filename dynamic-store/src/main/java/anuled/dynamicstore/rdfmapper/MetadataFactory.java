package anuled.dynamicstore.rdfmapper;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import anuled.dynamicstore.backend.Product;
import anuled.vocabulary.Geo;
import anuled.vocabulary.LED;
import anuled.vocabulary.QB;

/**
 * Utility class to generate Jena models for storing metadata associated with a
 * product.
 */
public class MetadataFactory {

	public static Model makeMetaModel(Product product, String uriPrefix) {
		Model metaModel = ModelFactory.createDefaultModel();

		// build qb:DimensionProperties
		Property time = metaModel.createProperty(uriPrefix + "time");
		time.addProperty(RDF.type, QB.DimensionProperty).addProperty(RDFS.range,
				XSD.dateTime);
		Property band = metaModel.createProperty(uriPrefix + "band");
		band.addProperty(RDF.type, QB.DimensionProperty).addProperty(RDFS.range,
				XSD.xint);
		Property dggsCell = metaModel.createProperty(uriPrefix + "dggsCell");
		dggsCell.addProperty(RDF.type, QB.DimensionProperty)
				.addProperty(RDFS.range, XSD.xstring);
		Property dggsLevelSquare = metaModel
				.createProperty(uriPrefix + "dggsLevelSquare");
		dggsLevelSquare.addProperty(RDF.type, QB.DimensionProperty)
				.addProperty(RDFS.range, XSD.xstring);
		Property dggsLevelPixel = metaModel
				.createProperty(uriPrefix + "dggsLevelPixel");
		dggsLevelPixel.addProperty(RDF.type, QB.DimensionProperty)
				.addProperty(RDFS.range, XSD.xstring);

		// build qb:ComponentSpecifications
		Resource positionComponent = metaModel
				.createResource(uriPrefix + "positionComponent")
				.addProperty(RDF.type, QB.ComponentSpecification)
				.addProperty(QB.dimension, LED.location);
		Resource latitudeComponent = metaModel
				.createResource(uriPrefix + "latitudeComponent")
				.addProperty(RDF.type, QB.ComponentSpecification)
				.addProperty(QB.dimension, Geo.lat);
		Resource longitudeComponent = metaModel
				.createResource(uriPrefix + "longitudeComponent")
				.addProperty(RDF.type, QB.ComponentSpecification)
				.addProperty(QB.dimension, Geo.long_);
		Resource timeComponent = metaModel
				.createResource(uriPrefix + "timeComponent")
				.addProperty(RDF.type, QB.ComponentSpecification)
				.addProperty(QB.dimension, time);
		Resource bandComponent = metaModel
				.createResource(uriPrefix + "bandComponent")
				.addProperty(RDF.type, QB.ComponentSpecification)
				.addProperty(QB.dimension, band);
		Resource dataComponent = metaModel
				.createResource(uriPrefix + "dataComponent")
				.addProperty(RDF.type, QB.ComponentSpecification)
				.addProperty(QB.dimension, LED.imageData);
		Resource dggsCellComponent = metaModel
				.createResource(uriPrefix + "dggsCellComponent")
				.addProperty(RDF.type, QB.ComponentSpecification)
				.addProperty(QB.dimension, dggsCell);
		Resource dggsLevelSquareComponent = metaModel
				.createResource(uriPrefix + "dggsLevelSquareComponent")
				.addProperty(RDF.type, QB.ComponentSpecification)
				.addProperty(QB.dimension, dggsLevelSquare);
		Resource dggsLevelPixelComponent = metaModel
				.createResource(uriPrefix + "dggsLevelPixelComponent")
				.addProperty(RDF.type, QB.ComponentSpecification)
				.addProperty(QB.dimension, dggsLevelPixel);

		// build qb:DataStructureDefinition
		Resource dsd = metaModel.createResource(uriPrefix + "structure")
				.addProperty(RDF.type, QB.DataStructureDefinition)
				.addProperty(QB.component, positionComponent)
				.addProperty(QB.component, latitudeComponent)
				.addProperty(QB.component, longitudeComponent)
				.addProperty(QB.component, timeComponent)
				.addProperty(QB.component, bandComponent)
				.addProperty(QB.component, dataComponent)
				.addProperty(QB.component, dggsCellComponent)
				.addProperty(QB.component, dggsLevelSquareComponent)
				.addProperty(QB.component, dggsLevelPixelComponent);

		// build qb:DataSet with predictable URI
		// TODO: Need to test this.
		metaModel.add(product.getFreeFormMetadata());
		Resource qbDataSet = metaModel.createResource(datasetURI(uriPrefix, product))
				.addProperty(RDF.type, QB.DataSet)
				.addProperty(QB.structure, dsd);
		StmtIterator dsIterator = product.getQBDSAttributes().listProperties();
		while (dsIterator.hasNext()) {
			Statement stmt = dsIterator.next();
			qbDataSet.addProperty(stmt.getPredicate(), stmt.getObject());
		}

		return metaModel;
	}

	/** Get the URI for a qb:Dataset */
	public static String datasetURI(String rootPrefix, Product product) {
		return rootPrefix + product.getName() + "-dataset";
	}
}
