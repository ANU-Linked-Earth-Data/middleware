package anuled.dynamicstore.rdfmapper;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;

import org.junit.Test;

import anuled.dynamicstore.rdfmapper.URLScheme;

public class TestURLScheme {
	@Test
	public void testParseURL() {
		String[] failureCases = { "https://anulinkedearth.org/rdf/2012/03/",
				"http://google.com/", "!*(malformed(*A" };
		for (String failureCase : failureCases) {
			try {
				URLScheme.parseObservationURL(failureCase);
			} catch (URLScheme.ParseException e) {
				continue;
			}
			assertTrue("String " + failureCase + " should not have parsed",
					false);
		}

		String passCase = "https://anulinkedearth.org/rdf/2012/03/02/23/13/42/cell/R91/"
				+ "levelSquare-3/levelPixel-4/band-5";
		URLScheme.ObservationMeta meta = URLScheme
				.parseObservationURL(passCase);
		assertEquals("R91", meta.cell);
		assertEquals(3, meta.levelSquare);
		assertEquals(4, meta.levelPixel);
		assertEquals(5, meta.band);
		assertEquals(ZonedDateTime.parse("2012-03-02T23:13:42Z"),
				meta.timestamp);
	}
}
