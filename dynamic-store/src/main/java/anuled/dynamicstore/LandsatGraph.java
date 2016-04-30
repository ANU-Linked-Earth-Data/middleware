package anuled.dynamicstore;

import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

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
	private Model dataCubeMeta;
	private Resource qbStructure;

	public LandsatGraph() {
		super();
		final String prefix = "http://www.example.org/ANU-LED-example#";
		dataCubeMeta = ModelFactory.createDefaultModel();
		qbStructure = dataCubeMeta.createResource(prefix + "landsatDataStructure")
				.addProperty(RDF.type, QB.DataStructureDefinition);
		dataCubeMeta.createResource(prefix + "landsatData")
				.addProperty(RDF.type, QB.DataSet)
				.addProperty(QB.structure, qbStructure);
	}

	@Override
	protected ExtendedIterator<Triple> graphBaseFind(Triple trip) {
		StmtIterator metaStmts = dataCubeMeta.listStatements();
		// TODO: Concatenate with iterator over triples from data itself
		return metaStmts.mapWith(stmt -> stmt.asTriple());
	}

}
