package anuled.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * Linked Earth data vocabulary
 *
 */
public class LED extends AbstractVocabulary {

	static {
		uri = "http://www.anulinkedearth.org/sandbox/ANU-LED#";
	}

	// Classes
	public static final Resource Pixel = resource("Pixel");
	public static final Resource GridSquare = resource("GridSquare");
	public static final Resource pixelsPerDegree = resource("pixelsPerDegree");
	
	// Properties
	public static final Property imageData = property("imageData");
	public static final Property value = property("value");
	public static final Property location = property("location");
	public static final Property bounds = property("bounds");
	public static final Property resolution = property("resolution");
	public static final Property containedInSquare = property("containedInSquare");
	public static final Property containsSquare = property("containsSquare");
	public static final Property time = property("time");
	public static final Property dggsCell = property("dggsCell");
	public static final Property dggsLevelSquare = property("dggsLevelSquare");
	public static final Property dggsLevelPixel = property("dggsLevelPixel");
	public static final Property etmBand = property("etmBand");
	
	// These are for Jena and the middleware
	public static final Property uriPrefix = property("uriPrefix");
	public static final Property hdf5Path = property("hdf5Path");
	public static final Property qbCovDataset = property("qbCovDataset");
	public static final Resource QBCovDataset = resource("QBCovDataset");
	public static final Resource QBCovObservationGraph = resource("QBCovObservationGraph");
	public static final Resource QBCovMetaGraph = resource("QBCovMetaGraph");
}
