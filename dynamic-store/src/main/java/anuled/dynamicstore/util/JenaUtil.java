package anuled.dynamicstore.util;

import java.util.Optional;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.ResourceFactory;

public class JenaUtil {
	public static Node createLiteralNode(Object value) {
		return ResourceFactory.createTypedLiteral(value).asNode();
	}

	public static Node createLiteralNode(String value, String typeURI) {
		RDFDatatype type = new BaseDatatype(typeURI);
		return NodeFactory.createLiteral(value, type);
	}

	public static Node createURINode(String uri) {
		return NodeFactory.createURI(uri);
	}
	
	public static Optional<Double> toDouble(Node node) {
		if (node.isLiteral()) {
			// This seems like the most flexible approach
			String lv = node.getLiteralLexicalForm();
			try {
				return Optional.of(Double.parseDouble(lv));
			} catch (NumberFormatException e) {}
		}
		return Optional.empty();
	}
}
