package anuled.dynamicstore;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.DisjointUnion;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import anuled.dynamicstore.backend.HDF5Dataset;

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
public class HighLevelDataset {
	private String uriPrefix, hdf5Path;
	private HDF5Dataset backingDataset;
	private ObservationGraph observationGraph;
	private Model metaModel = ModelFactory.createDefaultModel();
	private Graph unionGraph;
	
	public HighLevelDataset(String hdf5Path, String uriPrefix) {
		this.hdf5Path = hdf5Path;
		this.uriPrefix = uriPrefix;
		
		backingDataset = new HDF5Dataset(this.hdf5Path);
		
		buildMetaModel();
		observationGraph = new ObservationGraph(backingDataset);

		unionGraph = new DisjointUnion(metaModel.getGraph(), observationGraph);
	}
	
	private void buildMetaModel() {
		// build qb:ComponentSpecifications
		
		// build qb:DimensionProperties
		
		// build qb:AttributeProperties
		
		// build qb:DataStructureDefinition
		
		// build qb:DataSet
	}
	
	public Graph getQBGraph() {
		return unionGraph;
	}
	
	public String getPrefix() {
		return uriPrefix;
	}
}
