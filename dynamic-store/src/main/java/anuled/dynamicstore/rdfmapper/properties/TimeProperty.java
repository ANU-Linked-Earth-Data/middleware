package anuled.dynamicstore.rdfmapper.properties;

import java.util.GregorianCalendar;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Resource;

import anuled.dynamicstore.backend.Cell;
import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.util.JenaUtil;
import anuled.vocabulary.LED;

public class TimeProperty implements ObservationProperty {

	@Override
	public String getURI() {
		return LED.time.getURI();
	}

	@Override
	public Stream<Resource> valuesForObservation(Observation obs) {
		Cell cell = obs.getCell();
		GregorianCalendar obsTimestamp = GregorianCalendar
				.from(cell.getDataset().getTimestamp().toZonedDateTime());
		return Stream.of(JenaUtil.createLiteralResource(obsTimestamp));
	}

}
