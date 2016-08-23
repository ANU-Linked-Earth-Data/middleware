package anuled.dynamicstore;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.query.ARQ;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.engine.main.StageBuilder;
import org.apache.jena.sparql.engine.main.StageGenerator;
import org.apache.jena.sparql.util.graph.GraphUtils;

import anuled.dynamicstore.sparqlopt.ObservationGraphStageGenerator;
import anuled.vocabulary.LED;

public class QBCovGraphAssembler extends AssemblerBase {
	private static boolean initialised = false;

	public static void init() {
		if (!initialised) {
			Assembler.general.implementWith(LED.QBCovGraph,
					new QBCovGraphAssembler());
			
			StageGenerator oldGenerator = (StageGenerator) ARQ.getContext()
					.get(ARQ.stageGenerator);
			StageGenerator newGenerator = new ObservationGraphStageGenerator(
					oldGenerator);
			StageBuilder.setGenerator(ARQ.getContext(), newGenerator);
			
			initialised = true;
		}
	}

	@Override
	public Model open(Assembler a, Resource root, Mode mode) {
		// As far as I can tell, exactlyOneProperty never actually returns
		// false. It just throws an exception when the condition isn't met. Why
		// that isn't documented is beyond me.
		GraphUtils.exactlyOneProperty(root, LED.hdf5Path);
		GraphUtils.exactlyOneProperty(root, LED.uriPrefix);
		String hdf5Path = GraphUtils.getAsStringValue(root, LED.hdf5Path);
		String uriPrefix = GraphUtils.getAsStringValue(root, LED.uriPrefix);
		QBCovDataset graph = new QBCovDataset(hdf5Path, uriPrefix);
		return ModelFactory.createModelForGraph(graph.getQBGraph());
	}
}
