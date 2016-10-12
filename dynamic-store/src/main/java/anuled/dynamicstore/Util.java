package anuled.dynamicstore;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.ResourceFactory;

public class Util {
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
			} catch (NumberFormatException e) {
			}
		}
		return Optional.empty();
	}

	private static DateTimeFormatter canonicalFormatter = DateTimeFormatter
			.ofPattern("yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'");

	/**
	 * ISO8601 can map the same date and time to many different string
	 * representations. This method tries to settle on a sane default (which
	 * happens to match Python's default, albeit more because that's convenient
	 * than because Python's default is OMG AMAZING).
	 */
	public static String canonicalTimeString(ZonedDateTime time) {
		// Target format is "2013-05-27T23:58:20+00:00".
		ZonedDateTime asUTC = time.withZoneSameInstant(ZoneOffset.UTC);
		return asUTC.format(canonicalFormatter);
	}
}
