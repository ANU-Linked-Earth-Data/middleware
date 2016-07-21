package anuled.dynamicstore.rdfmapper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import anuled.dynamicstore.backend.Cell;
import anuled.dynamicstore.backend.Observation;

/**
 * Code for formatting and parsing using our observation URL scheme.
 */
public class URLScheme {
	// eventually this will have to be configurable
	public static String DATA_PREFIX = "https://anulinkedearth.org/rdf/";
	public static Pattern SUFFIX_PATTERN = Pattern
			.compile("^(?<year>\\d{4})/(?<month>\\d{2})/(?<day>\\d{2})"
					+ "/(?<hour>\\d{2})/(?<minute>\\d{2})/(?<second>\\d{2})"
					+ "/cell/(?<cellID>[N-S]\\d*)"
					+ "/levelSquare-(?<levelSquare>\\d+)"
					+ "/levelPixel-(?<levelPixel>\\d+)"
					+ "/band-(?<band>\\d+)$");

	/**
	 * Get the URL corresponding to a specific observation (cell + band +
	 * resolution) in the dataset.
	 */
	public static String observationURL(Observation obs) {
		String rv = DATA_PREFIX + "observation";

		// Add date/time
		Cell cell = obs.getCell();
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

	public static class ParseException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public ParseException(String msg) {
			super(msg);
		}
	}

	public static class ObservationMeta {
		int levelSquare, levelPixel, band;
		ZonedDateTime timestamp;
		String cell;
	}

	public static ObservationMeta parseObservationURL(String url) {
		if (!url.startsWith(DATA_PREFIX)) {
			throw new ParseException("URL '" + url + "' does not start with '"
					+ DATA_PREFIX + "'");
		}
		String suffix = url.substring(DATA_PREFIX.length());
		return parseObservationURLSuffix(suffix);
	}

	private static ObservationMeta parseObservationURLSuffix(String suffix) {
		Matcher matcher = SUFFIX_PATTERN.matcher(suffix);
		if (!matcher.matches()) {
			throw new ParseException("Invalid suffix: '" + suffix + "'");
		}
		ObservationMeta rv = new ObservationMeta();
		int year = Integer.parseInt(matcher.group("year")),
				month = Integer.parseInt(matcher.group("month")),
				day = Integer.parseInt(matcher.group("day")),
				hour = Integer.parseInt(matcher.group("hour")),
				minute = Integer.parseInt(matcher.group("minute")),
				second = Integer.parseInt(matcher.group("second"));
		rv.timestamp = ZonedDateTime.of(year, month, day, hour, minute, second,
				0, ZoneOffset.UTC);
		rv.cell = matcher.group("cellID");
		rv.levelSquare = Integer.parseInt(matcher.group("levelSquare"));
		rv.levelPixel = Integer.parseInt(matcher.group("levelPixel"));
		rv.band = Integer.parseInt(matcher.group("band"));
		return rv;
	}
}
