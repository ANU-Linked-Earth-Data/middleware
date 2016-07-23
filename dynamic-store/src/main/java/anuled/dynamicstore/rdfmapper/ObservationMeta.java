package anuled.dynamicstore.rdfmapper;

import java.time.ZonedDateTime;

/** Represents metadata which completely identifies an observation */
public class ObservationMeta {
	int levelSquare, levelPixel, band;
	ZonedDateTime timestamp;
	String cell;
}