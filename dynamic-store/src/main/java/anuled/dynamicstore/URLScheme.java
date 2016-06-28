package anuled.dynamicstore;

import java.time.OffsetDateTime;

/**
 * Code for formatting and parsing using our observation URL scheme.
 */
public class URLScheme {
	// eventually this will have to be configurable
	public static String DATA_PREFIX = "https://anulinkedearth.org/rdf/";

	/**
	 * Get the URL corresponding to a specific observation (cell + band +
	 * resolution) in the dataset.
	 */
	public static String observationURL(HDF5Dataset.Observation obs) {
		String rv = DATA_PREFIX + "observation";

		// Add date/time
		HDF5Dataset.Cell cell = obs.getCell();
		OffsetDateTime dt = cell.getDataset().getTimestamp();
		rv += String.format("/%04d/%02d/%02d/%02d/%02d/%02d", dt.getYear(),
				dt.getMonthValue(), dt.getDayOfMonth(), dt.getHour(),
				dt.getMinute(), dt.getSecond());

		// DGGS details
		rv += String.format("/cell/%s/levelSquare-%d/levelPixel-%d/band-%d",
				cell.getDGGSIdent(), obs.getCellLevel(), obs.getPixelLevel(),
				obs.getBand());
		return rv;
	}

	public class ObservationInfo {
	}

	public static ObservationInfo parseURL(String url) {
		// TODO
		return null;
	}
}
