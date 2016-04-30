package anuled.dynamicstore;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.ModelCom;

/**
 * Jena version of hello world
 */
public class App {
	public static void main(String[] args) {
		LandsatGraph graph = new LandsatGraph();
		Model model = new ModelCom(graph);
		model.write(System.out, "TURTLE");
	}
}
