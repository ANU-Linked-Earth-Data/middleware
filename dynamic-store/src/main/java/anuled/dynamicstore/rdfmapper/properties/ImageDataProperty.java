package anuled.dynamicstore.rdfmapper.properties;

import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Resource;

import anuled.dynamicstore.backend.Observation;
import anuled.dynamicstore.backend.TileObservation;
import anuled.dynamicstore.util.ImageUtil;
import anuled.dynamicstore.util.JenaUtil;
import anuled.vocabulary.LED;

public class ImageDataProperty implements ObservationProperty {

	@Override
	public String getURI() {
		return LED.imageData.getURI();
	}

	@Override
	public Stream<Resource> valuesForObservation(Observation obs) {
		if (obs instanceof TileObservation){
			TileObservation tileObs = (TileObservation) obs;
			short invalidValue = tileObs.getCell().getInvalidValue();
			BufferedImage tileImage = ImageUtil.arrayToImage(tileObs.getTile(),
					invalidValue);
			String dataURI = ImageUtil.imageToPNGURL(tileImage);
			return Stream.of(JenaUtil.createURIResource(dataURI));
		}
		// For PixelObservations, we can't do anything
		return Stream.of();
	}

}
