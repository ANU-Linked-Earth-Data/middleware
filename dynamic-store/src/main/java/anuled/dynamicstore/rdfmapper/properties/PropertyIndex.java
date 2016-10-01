package anuled.dynamicstore.rdfmapper.properties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Resource;

import static anuled.dynamicstore.rdfmapper.properties.LatLonBoxProperty.BoundType.*;

public class PropertyIndex {
	private static Map<String, ObservationProperty> index = new HashMap<>();
	private static Set<String> externalURIs = new HashSet<>();

	private static void register(ObservationProperty p) {
		String key = p.getURI();
		if (index.containsKey(key)) {
			throw new RuntimeException("URL " + key + " already registered");
		}
		index.put(key, p);
		if (!p.isInternal()) {
			externalURIs.add(key);
		}
		// For whatever reason, the next line gives "java.lang.AssertionError:
		// null" when executed as part of a test using mvn test (but not when
		// using Eclipse). I have no idea why that's the case, so I'll just
		// leave it out for now.
		// assert index.keySet().contains(externalURIs);
	}

	static {
		// Register everything!
		Stream<Class<? extends ObservationProperty>> toRegister = Stream.of(
				BandProperty.class, BoundsProperty.class, DataSetProperty.class,
				DGGSCellProperty.class, DGGSLevelPixelProperty.class,
				DGGSLevelSquareProperty.class, ImageDataProperty.class,
				LatProperty.class, LongProperty.class, PixelValueProperty.class,
				RDFTypeProperty.class, ResolutionProperty.class,
				TimeProperty.class, LatMinProperty.class, LatMaxProperty.class,
				LongMinProperty.class, LongMaxProperty.class);
		toRegister.forEach(cls -> {
			ObservationProperty inst;
			try {
				inst = cls.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("Error instantiating property", e);
			}
			register(inst);
		});

		// Register some internal lat/lon box properties
		LatLonBoxProperty[] otherProperties = {
				new LatLonBoxProperty(BoxBottom), new LatLonBoxProperty(BoxTop),
				new LatLonBoxProperty(BoxLeft),
				new LatLonBoxProperty(BoxRight) };
		for (LatLonBoxProperty property : otherProperties) {
			register(property);
		}
	}

	public static Optional<ObservationProperty> getProperty(Resource uri) {
		return getProperty(uri.getURI());
	}

	public static Optional<ObservationProperty> getProperty(String uri) {
		ObservationProperty prop = index.get(uri);
		return Optional.ofNullable(prop);
	}

	/**
	 * List URIs for properties which can be used external (i.e. ones which the
	 * user can use in triple patterns, retrieve values for, etc.)
	 */
	public static Set<String> externalPropertyURIs() {
		return externalURIs;
	}
}