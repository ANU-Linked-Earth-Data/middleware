package anuled.dynamicstore.util;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class JenaUtil {
	public static Resource createLiteralResource(Object value) {
		return ResourceFactory.createTypedLiteral(value).asResource();
	}

	public static Resource createLiteralResource(String value, String typeURI) {
		RDFDatatype type = new BaseDatatype(typeURI);
		return ResourceFactory.createTypedLiteral(value, type)
				.asResource();
	}

	public static Resource createURIResource(String URI) {
		return ResourceFactory.createResource(URI);
	}
}
