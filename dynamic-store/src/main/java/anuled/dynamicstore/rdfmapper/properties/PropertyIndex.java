package anuled.dynamicstore.rdfmapper.properties;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Resource;

public class PropertyIndex {
	private static Map<String, ObservationProperty> index = new HashMap<String, ObservationProperty>();

	private static void register(ObservationProperty p) {
		String key = p.getURI();
		if (index.containsKey(key)) {
			throw new RuntimeException("URL " + key + " already registered");
		}
		index.put(key, p);
	}

	static {
		// Register everything!
		Stream<Class<? extends ObservationProperty>> toRegister = Stream.of(
				BandProperty.class, BoundsProperty.class, DataSetProperty.class,
				DGGSCellProperty.class, DGGSLevelPixelProperty.class,
				DGGSLevelSquareProperty.class, ImageDataProperty.class,
				LatProperty.class, LongProperty.class, PixelValueProperty.class,
				RDFTypeProperty.class, ResolutionProperty.class,
				TimeProperty.class
		);
		toRegister.forEach(cls -> {
			ObservationProperty inst;
			try {
				inst = cls.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("Error instantiating property", e);
			}
			register(inst);
		});
	}
	
	public static ObservationProperty getProperty(Resource uri) {
		return getProperty(uri.getURI());
	}

	public static ObservationProperty getProperty(String uri) {
		return index.get(uri);
	}
	
	public static Set<String> propertyURIs() {
		return index.keySet();
	}
}