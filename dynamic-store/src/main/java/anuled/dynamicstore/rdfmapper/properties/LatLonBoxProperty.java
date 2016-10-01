package anuled.dynamicstore.rdfmapper.properties;

import static anuled.dynamicstore.util.JenaUtil.*;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;

import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.rdfmapper.ObservationFilter;

/**
 * This class implements a pseudo-property intended to put minimum and maximum
 * constraints on led:l{at,on}M{in,ax}. Doing so is useful for implementing
 * filter-by-MBR on qb:Observations. The pseudo-property described here is not
 * intended to be used externally (although it could be, conceivably). Rather,
 * the intention is that these properties will be inserted by the rest of the
 * middleware, as directed by the relevant bindings and filter expressions.
 * 
 * This approach is horribly hacky. I used it because it lends itself to quick
 * and easy implementation. If this codebase is ever extended to support other
 * spatial filtering operations (it probably won't be), then this should be one
 * of the first classes to go :-)
 */
public class LatLonBoxProperty implements ObservationProperty {
	private static String propertyGUID = UUID.randomUUID().toString();

	public static enum BoundType {
		// latMin must be >= value
		BoxBottom,
		// latMax must be <= value
		BoxTop,
		// lonMin must be >= value
		BoxLeft,
		// lonMax must be <= value
		BoxRight;

		// intentionally break code that relies on this URI by incorporating a
		// runtime-generated GUID
		private String uri = "x-anu-led:" + this.name() + "lat-lon-box-"
				+ propertyGUID;

		public String getURI() {
			return uri;
		}
	}

	private BoundType type;
	private String uri;

	public LatLonBoxProperty(BoundType type) {
		this.type = type;
		this.uri = type.getURI();
	}

	@Override
	public String getURI() {
		return uri;
	}

	@Override
	public Stream<Node> valuesForObservation(Observation obs) {
		// Could return an empty stream, but I think it's better to break
		// entirely. This code really should never be called.
		throw new RuntimeException("Property is internal, and not intended "
				+ "to be used to generate bindings. Are you sure your code "
				+ "is correct?");
	}

	@Override
	public void applyToFilter(ObservationFilter filter, Node value) {
		Optional<Double> realVal = toDouble(value);
		if (realVal.isPresent()) {
			double limit = realVal.get();
			switch (type) {
			case BoxBottom:
				filter.constrainLatMin(limit);
				break;
			case BoxTop:
				filter.constrainLatMax(limit);
				break;
			case BoxLeft:
				filter.constrainLonMin(limit);
				break;
			case BoxRight:
				filter.constrainLonMax(limit);
				break;
			default:
				throw new RuntimeException("Unhandled type " + type);
			}
		} else {
			filter.constrainImpossibly();
		}
	}

	@Override
	public boolean isInternal() {
		return true;
	}
}
