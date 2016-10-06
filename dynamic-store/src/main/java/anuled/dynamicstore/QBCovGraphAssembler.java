package anuled.dynamicstore;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.query.ARQ;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.engine.main.StageBuilder;
import org.apache.jena.sparql.engine.main.StageGenerator;
import org.apache.jena.sparql.util.graph.GraphUtils;

import anuled.dynamicstore.sparqlopt.ObservationGraphOpExecutor;
import anuled.dynamicstore.sparqlopt.ObservationGraphStageGenerator;
import anuled.vocabulary.LED;

public class QBCovGraphAssembler extends AssemblerBase {
	private static boolean initialised = false;

	private static QBCovDataset getDataset(Assembler asm, Resource root) {
		GraphUtils.exactlyOneProperty(root, LED.qbCovDataset);
		// Modelled off Lucene code, which also uses assemblers to construct
		// things which aren't models (and has to access them later somehow!)
		// https://github.com/apache/jena/blob/c7b83dbe6490fb67a33d55ce2b5b06ba07646e15/jena-text/src/main/java/org/apache/jena/query/text/assembler/EntityDefinitionAssembler.java
		return (QBCovDataset) asm.open(
				root.getProperty(LED.qbCovDataset).getObject().asResource());
	}

	public static void init() {
		if (!initialised) {
			Assembler.general.implementWith(LED.QBCovDataset,
					new QBCovGraphAssembler());

			// Metadata graph assembler
			Assembler.general.implementWith(LED.QBCovObservationGraph,
					new AssemblerBase() {
						@Override
						public Model open(Assembler a, Resource root,
								Mode mode) {
							return ModelFactory.createModelForGraph(
									getDataset(a, root).getObservationGraph());
						}
					});

			// Observation graph assembler
			Assembler.general.implementWith(LED.QBCovMetaGraph,
					new AssemblerBase() {
						@Override
						public Model open(Assembler a, Resource root,
								Mode mode) {
							return ModelFactory.createModelForGraph(
									getDataset(a, root).getMetaGraph());
						}
					});

			// XXX: Registering these two classes globally is a Bad Idea(tm).
			// If I can, I'd like to add them to relevant contexts (e.g. in
			// production, in unit tests) manually.

			// as good a place as any to put this in
			// now we only have to load one custom class in our Fuseki configs
			StageGenerator oldGenerator = (StageGenerator) ARQ.getContext()
					.get(ARQ.stageGenerator);
			StageGenerator newGenerator = new ObservationGraphStageGenerator(
					oldGenerator);
			StageBuilder.setGenerator(ARQ.getContext(), newGenerator);

			// Also load our filter-handling opExecutor
			QC.setFactory(ARQ.getContext(), new OpExecutorFactory() {
				@Override
				public OpExecutor create(ExecutionContext execCtx) {
					return new ObservationGraphOpExecutor(execCtx);
				}
			});

			initialised = true;
		}
	}

	@Override
	public QBCovDataset open(Assembler a, Resource root, Mode mode) {
		// As far as I can tell, exactlyOneProperty never actually returns
		// false. It just throws an exception when the condition isn't met. Why
		// that isn't documented is beyond me.
		GraphUtils.exactlyOneProperty(root, LED.hdf5Path);
		GraphUtils.exactlyOneProperty(root, LED.uriPrefix);
		String hdf5Path = GraphUtils.getAsStringValue(root, LED.hdf5Path);
		String uriPrefix = GraphUtils.getAsStringValue(root, LED.uriPrefix);
		return new QBCovDataset(hdf5Path, uriPrefix);
	}
}
