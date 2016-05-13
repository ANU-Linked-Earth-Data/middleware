package anuled.dynamicstore;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.ModelCom;

/**
 * Jena version of hello world
 */
public class App {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println(
					"Need a path to a GeoTIFF tile, and no other arguments");
			System.exit(1);
		}
		LandsatGraph graph = new LandsatGraph(args[0]);
		Model model = new ModelCom(graph);
		model.write(System.out, "TURTLE");
	}
}
