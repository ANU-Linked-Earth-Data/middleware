package anuled.dynamicstore.backend;

import java.time.ZonedDateTime;

import ch.systemsx.cisd.hdf5.IHDF5Reader;

/**
 * <code>Observation</code> subclass for single pixel observations. In this
 * context "pixel" means a single value summarising the contents of an entire
 * DGGS cell (e.g. the green level averaged over the whole extent of the cell).
 */
public final class PixelObservation extends Observation {
	protected PixelObservation(Cell cell, Product product,
			ZonedDateTime timestamp, int band) {
		super(cell, product, timestamp, band);
	}

	public int getPixelLevel() {
		return getCellLevel();
	}

	public double getPixel() {
		IHDF5Reader fp = cell.getReader();
		String dsPath = cell.getPath() + "/" + product.getName() + "/pixel"
				+ "@" + timestamp.toString();
		return fp.readDoubleArray(dsPath)[band];
	}

	public double getResolution() {
		return 1.0 / getCell().getDegreesSpanned();
	}

	@Override
	public boolean equals(Object other) {
		return super.equals(other) && (other instanceof PixelObservation);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ PixelObservation.class.hashCode();
	}

	@Override
	public String toString() {
		return super.toString() + ", type=pixel";
	}
}
