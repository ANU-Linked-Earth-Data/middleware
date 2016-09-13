package anuled.dynamicstore.util;

import java.util.Iterator;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIteratorBase;
import org.apache.jena.sparql.serializer.SerializationContext;

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

	public static QueryIterFromIterator createQueryIterator(
			Iterator<Binding> iterator) {
		return new QueryIterFromIterator(iterator);
	}

	/**
	 * Wrapper to turn a <code>Iterator&lt;Binding&gt;</code> into a
	 * <code>QueryIterator</code>. I'm really surprised Jena doesn't have
	 * this&mdash;maybe I missed it?
	 */
	public static class QueryIterFromIterator extends QueryIteratorBase {
		private Iterator<Binding> bindings;

		public QueryIterFromIterator(Iterator<Binding> bindings) {
			this.bindings = bindings;
		}

		@Override
		public void output(IndentedWriter out, SerializationContext sCxt) {
			output(out, null);
		}

		@Override
		protected boolean hasNextBinding() {
			return bindings.hasNext();
		}

		@Override
		protected Binding moveToNextBinding() {
			return bindings.next();
		}

		@Override
		protected void closeIterator() {
			// noop
		}

		@Override
		protected void requestCancel() {
			// noop
		}

	}
}
