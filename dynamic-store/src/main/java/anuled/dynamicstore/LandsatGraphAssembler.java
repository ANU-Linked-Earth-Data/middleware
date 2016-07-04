package anuled.dynamicstore;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.util.graph.GraphUtils;
import anuled.vocabulary.LED;

public class LandsatGraphAssembler extends AssemblerBase {
	private static boolean initialised = false;
	
	public static void init() {
		if (!initialised) {
			Assembler.general.implementWith(LED.LandsatGraph, new LandsatGraphAssembler());
			initialised = true;
		}
	}

	@Override
	public Model open(Assembler a, Resource root, Mode mode) {
		if (!GraphUtils.exactlyOneProperty(root, LED.hdf5Path)) {
			throw new AssemblerException(root, "Require a single led:hdf5Path for source dataset");
		}
		String hdf5Path = GraphUtils.getAsStringValue(root, LED.hdf5Path);
		LandsatGraph graph = new LandsatGraph(hdf5Path);
		return ModelFactory.createModelForGraph(graph);
	}

}
