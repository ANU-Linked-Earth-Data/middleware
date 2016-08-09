package anuled.dynamicstore;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.util.graph.GraphUtils;
import anuled.vocabulary.LED;

public class ObservationGraphAssembler extends AssemblerBase {
	private static boolean initialised = false;

	public static void init() {
		if (!initialised) {
			Assembler.general.implementWith(LED.ObservationGraph,
					new ObservationGraphAssembler());
			initialised = true;
		}
	}

	@Override
	public Model open(Assembler a, Resource root, Mode mode) {
		// As far as I can tell, exactlyOneProperty never actually returns
		// false. It just throws an exception when the condition isn't met. Why
		// that isn't documented is beyond me.
		GraphUtils.exactlyOneProperty(root, LED.hdf5Path);
		String hdf5Path = GraphUtils.getAsStringValue(root, LED.hdf5Path);
		// FIXME: yes, this is broken. I really need to get rid of this assembler, though
		ObservationGraph graph = new ObservationGraph(hdf5Path, "http://something");
		return ModelFactory.createModelForGraph(graph);
	}
}
