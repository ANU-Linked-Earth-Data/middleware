package anuled.dynamicstore.backend;

import java.time.ZonedDateTime;

import anuled.dynamicstore.Util;
import ch.systemsx.cisd.hdf5.IHDF5ByteReader;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

/**
 * <code>Observation</code> subclass for entire image tiles. Unlike
 * <code>PixelObservation</code>, this class uses a square grid of many pixels
 * to summarise cell contents.
 */
public final class TileObservation extends Observation {
	protected TileObservation(Cell cell, Product product,
			ZonedDateTime timestamp, int band) {
		super(cell, product, timestamp, band);
	}

	public int getPixelLevel() {
		int offset = (int) Math
				.round(Math.log(product.getTileSize()) / Math.log(3));
		return getCellLevel() + offset;
	}

	/** Return contents of tile as a byte array of PNG data */
	public byte[] getTile() {
		IHDF5Reader fp = cell.getReader();
		IHDF5ByteReader dataReader = fp.uint8();
		String dsPath = cell.getPath() + "/" + product.getName() + "/png_band_"
				+ band + "@" + Util.canonicalTimeString(timestamp);
		return dataReader.readArray(dsPath);
	}

	public double getResolution() {
		return product.getTileSize() / getCell().getDegreesSpanned();
	}

	@Override
	public boolean equals(Object other) {
		return super.equals(other) && (other instanceof TileObservation);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ TileObservation.class.hashCode();
	}

	@Override
	public String toString() {
		return super.toString() + ", type=tile";
	}
}
