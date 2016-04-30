package anuled.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * RDF data cube vocabulary
 *
 */
public class QB extends AbstractVocabulary {
	static {
		// Don't want to make overriding static stuff easy, huh Java? Well guess
		// what? I WIN!
		uri = "http://purl.org/linked-data/cube#";
	}

	// Classes
	public static final Resource Attachable = resource("Attachable");
	public static final Resource AttributeProperty = resource("AttributeProperty");
	public static final Resource CodedProperty = resource("CodedProperty");
	public static final Resource ComponentProperty = resource("ComponentProperty");
	public static final Resource ComponentSet = resource("ComponentSet");
	public static final Resource ComponentSpecification = resource("ComponentSpecification");
	public static final Resource DataSet = resource("DataSet");
	public static final Resource DataStructureDefinition = resource("DataStructureDefinition");
	public static final Resource DimensionProperty = resource("DimensionProperty");
	public static final Resource HierarchicalCodeList = resource("HierarchicalCodeList");
	public static final Resource MeasureProperty = resource("MeasureProperty");
	public static final Resource Observation = resource("Observation");
	public static final Resource Slice = resource("Slice");
	public static final Resource ObservationGroup = resource("ObservationGroup");
	public static final Resource SliceKey = resource("SliceKey");

	// Properties
	public static final Property attribute = property("attribute");
	public static final Property codeList = property("codeList");
	public static final Property component = property("component");
	public static final Property componentAttachment = property("componentAttachment");
	public static final Property componentProperty = property("componentProperty");
	public static final Property componentRequired = property("componentRequired");
	public static final Property concept = property("concept");
	public static final Property dataSet = property("dataSet");
	public static final Property dimension = property("dimension");
	public static final Property hierarchyRoot = property("hierarchyRoot");
	public static final Property measure = property("measure");
	public static final Property measureDimension = property("measureDimension");
	public static final Property measureType = property("measureType");
	public static final Property observation = property("observation");
	public static final Property observationGroup = property("observationGroup");
	public static final Property order = property("order");
	public static final Property parentChildProperty = property("parentChildProperty");
	public static final Property slice = property("slice");
	public static final Property sliceKey = property("sliceKey");
	public static final Property sliceStructure = property("sliceStructure");
	public static final Property structure = property("structure");
}
