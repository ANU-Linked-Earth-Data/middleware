package anuled.dynamicstore;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.DisjointUnion;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import anuled.dynamicstore.backend.HDF5Dataset;
import anuled.vocabulary.GCMDInstrument;
import anuled.vocabulary.GCMDPlatform;
import anuled.vocabulary.Geo;
import anuled.vocabulary.LED;
import anuled.vocabulary.QB;

/**
 * Class which encapsulates the configuration for a single dataset; for us,
 * "dataset" will likely mean a particular combination of satellite and
 * observation time (e.g. one dataset for Landsat observations in July 2016, one
 * for MODIS observations in October 2014, etc.).
 * 
 * This class has an associated <code>HDF5Dataset</code> which stores the actual
 * pixel data, as well as metadata about band counts, satellite names, etc. It
 * also has a prefix URL for the virtual RDF data cube graph associated with the
 * dataset (e.g. <code>https://example.org/data/landsat-june-2016/<code>).
 * 
 * From those two pieces of information, this class is able to instantiate two graphs:
 * 
 * <ol>
 * <li>A fully materialised graph which stores one-off metadata for an entire RDF data
 * cube: dataset definitions, <code>qb:DataSet</code>s, component
 * specifications, etc.</li>
 * <li>An <code>ObservationGrap</code> which is responsible for generating
 * actual <code>qb:Observation</code>s in responses to Jena API calls, SPARQL
 * queries, etc.</li>
 * </ol>
 * 
 * The union of these graphs yields the aforementioend virtual RDF data cube
 * graph.
 */
public class QBCovDataset {
	private String uriPrefix, hdf5Path;
	private HDF5Dataset backingDataset;
	private ObservationGraph observationGraph;
	private Model metaModel = ModelFactory.createDefaultModel();
	private Graph unionGraph;
	private Resource qbDataSet;

	public QBCovDataset(String hdf5Path, String uriPrefix) {
		this.hdf5Path = hdf5Path;
		this.uriPrefix = uriPrefix;

		backingDataset = new HDF5Dataset(this.hdf5Path);

		buildMetaModel();
		observationGraph = new ObservationGraph(backingDataset,
				qbDataSet.getURI());

		unionGraph = new DisjointUnion(metaModel.getGraph(), observationGraph);
	}

	private void buildMetaModel() {
		// build qb:AttributeProperties
		Property instrument = metaModel
				.createProperty(uriPrefix + "instrument");
		instrument.addProperty(RDF.type, QB.AttributeProperty)
				.addProperty(RDFS.range, XSD.xstring);
		Property satellite = metaModel.createProperty(uriPrefix + "satellite");
		satellite.addProperty(RDF.type, QB.AttributeProperty)
				.addProperty(RDFS.range, XSD.xstring);
		Property dggs = metaModel.createProperty(uriPrefix + "dggs");
		dggs.addProperty(RDF.type, QB.AttributeProperty).addProperty(RDFS.range,
				XSD.xstring);

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
		Resource satelliteComponent = metaModel
				.createResource(uriPrefix + "satelliteComponent")
				.addProperty(RDF.type, QB.ComponentSpecification)
				.addProperty(QB.dimension, satellite);
		Resource instrumentComponent = metaModel
				.createResource(uriPrefix + "instrumentComponent")
				.addProperty(RDF.type, QB.ComponentSpecification)
				.addProperty(QB.dimension, instrument);
		Resource bandComponent = metaModel
				.createResource(uriPrefix + "bandComponent")
				.addProperty(RDF.type, QB.ComponentSpecification)
				.addProperty(QB.dimension, band);
		Resource dataComponent = metaModel
				.createResource(uriPrefix + "dataComponent")
				.addProperty(RDF.type, QB.ComponentSpecification)
				.addProperty(QB.dimension, LED.imageData);
		Resource dggsComponent = metaModel
				.createResource(uriPrefix + "dggsComponent")
				.addProperty(RDF.type, QB.ComponentSpecification)
				.addProperty(QB.dimension, dggs);
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
				.addProperty(QB.component, satelliteComponent)
				.addProperty(QB.component, instrumentComponent)
				.addProperty(QB.component, bandComponent)
				.addProperty(QB.component, dataComponent)
				.addProperty(QB.component, dggsComponent)
				.addProperty(QB.component, dggsCellComponent)
				.addProperty(QB.component, dggsLevelSquareComponent)
				.addProperty(QB.component, dggsLevelPixelComponent);

		// build qb:DataSet
		qbDataSet = metaModel.createResource(uriPrefix + "dataset")
				.addProperty(RDF.type, QB.DataSet)
				.addProperty(QB.structure, dsd)
				.addProperty(instrument, GCMDInstrument.ETM)
				.addProperty(satellite, GCMDPlatform.LANDSAT_7)
				// Note: I /would/ have added a band attribute in here, but I'm
				// using it as a dimension instead (i.e. as something which is
				// carried with each observation rather than with the qb:Dataset
				.addLiteral(dggs, "rHEALPix WGS84 Ellipsoid");
	}

	public Graph getQBGraph() {
		return unionGraph;
	}

	public String getPrefix() {
		return uriPrefix;
	}
}
