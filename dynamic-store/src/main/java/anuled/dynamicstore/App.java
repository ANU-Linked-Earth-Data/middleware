package anuled.dynamicstore;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.ModelCom;

public class App {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println(
					"Need a path to an HDF5 tile, and no other arguments");
			System.exit(1);
		}
		ObservationGraph graph = new ObservationGraph(args[0]);
		Model model = new ModelCom(graph);
		Query q = QueryFactory
				.create("PREFIX qb: <http://purl.org/linked-data/cube#>\n"
						+ "SELECT ?s WHERE {?s a qb:Observation.} LIMIT 100");
		ResultSet rs = QueryExecutionFactory.create(q, model).execSelect();
		System.out.println("A selection of fine observation URIs:");
		while (rs.hasNext()) {
			System.out.println(rs.next());
		}
	}
}
