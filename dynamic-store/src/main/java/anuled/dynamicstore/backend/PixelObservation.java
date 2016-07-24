package anuled.dynamicstore.backend;

/**
 * <code>Observation</code> subclass for single pixel observations. In this
 * context "pixel" means a single value summarising the contents of an entire
 * DGGS cell (e.g. the green level averaged over the whole extent of the cell).
 */
public final class PixelObservation extends Observation {
	protected PixelObservation(Cell cell, int band) {
		super(cell, band);
	}

	public int getPixelLevel() {
		return getCellLevel();
	}

	public double getPixel() {
		return getCell().pixelData()[getBand()];
	}

	public double getResolution() {
		return 1.0 / getCell().getDegreesSpanned();
	}
	
	@Override
	public boolean equals(Object other) {
		return super.equals(other) && (other instanceof PixelObservation);
	}

	@Override
	public String toString() {
		return super.toString() + ", type=pixel";
	}
}
