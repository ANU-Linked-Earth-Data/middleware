package anuled.dynamicstore;

import java.time.OffsetDateTime;

/**
 * Code for formatting and parsing using our observation URL scheme.
 */
public class URLScheme {
	// eventually this will have to be configurable
	public static String DATA_PREFIX = "https://anulinkedearth.org/rdf/";

	public static String cellURL(HDF5Dataset.Observation obs) {
		String rv = DATA_PREFIX + "observation";

		// Add date/time
		HDF5Dataset.Cell cell = obs.getCell();
		OffsetDateTime dt = cell.getDataset().getTimestamp();
		rv += String.format("/%04i/%02i/%02i/%02i/%02i/%02i", dt.getYear(),
				dt.getMonthValue(), dt.getDayOfMonth(), dt.getHour(),
				dt.getMinute(), dt.getSecond());

		// DGGS details
		rv += String.format("/cell/%i/levelSquare-%i/levelPixel-%i/band-%i",
				cell.getDGGSIdent(), obs.getLevelSquare(), obs.getLevelPixel(),
				obs.getBand());
		return rv;
	}

	public class CellInfo {
	}

	public static CellInfo parseURL(String url) {
		// TODO
		return null;
	}
}
