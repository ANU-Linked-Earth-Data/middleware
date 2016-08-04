package anuled.dynamicstore.rdfmapper;

import java.time.ZonedDateTime;

/** Represents metadata which completely identifies an observation */
public class ObservationMeta implements Cloneable {
	int levelSquare, levelPixel, band;
	ZonedDateTime timestamp;
	String cell;
	
	public ObservationMeta clone() throws CloneNotSupportedException {
		return (ObservationMeta)super.clone();
	}
}