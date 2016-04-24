package anuled.dynamicstore;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * Jena version of hello world
 *
 */
public class App
{
    public static void main( String[] args )
    {
        Model model = ModelFactory.createDefaultModel();
        model.add(model.createStatement(
            model.createResource("http://this.hello/world#"),
            model.createProperty("http://foo.bar/baz#", "is"),
            model.createLiteral("Enterprise quality!", true)
        ));
        model.write(System.out, "RDF/XML-ABBREV");
    }
}
