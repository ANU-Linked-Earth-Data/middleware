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
}
