package anuled.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * W3C WGS84 location vocab
 *
 */
public class Geo extends AbstractVocabulary {
	static {
		uri = "https://www.w3.org/2003/01/geo/wgs84_pos#";
	}

	// Classes
	public static final Resource SpatialThing = resource("SpatialThing");
	public static final Resource Point = resource("Point");

	// Properties
	public static final Property lat = property("lat");
	public static final Property long_ = property("long");
	public static final Property alt = property("alt");
	public static final Property location = property("location");
	public static final Property lat_long = property("lat_long");
}
