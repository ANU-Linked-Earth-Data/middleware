package anuled.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Helper class for creating vocabularies. Emulates Jena's builtin vocab
 * classes.
 */
public abstract class AbstractVocabulary {
	protected static String uri = null;

	protected static final Resource resource(String local) {
		return ResourceFactory.createResource(uri + local);
	}

	protected static final Property property(String local) {
		return ResourceFactory.createProperty(uri, local);
	}
}
