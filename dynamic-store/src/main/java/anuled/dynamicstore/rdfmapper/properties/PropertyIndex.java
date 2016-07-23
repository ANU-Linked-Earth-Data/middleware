package anuled.dynamicstore.rdfmapper.properties;

import java.util.HashMap;
import java.util.Map;

public class PropertyIndex {
	private static Map<String, ObservationProperty> index = new HashMap<String, ObservationProperty>();
	
	static void register(ObservationProperty p) {
		String key = p.getURI();
		if (index.containsKey(key)) {
			throw new RuntimeException("URL " + key + " already registered");
		}
		index.put(key, p);
	}
	
	public static ObservationProperty getProperty(String uri) {
		return index.get(uri);
	}
}