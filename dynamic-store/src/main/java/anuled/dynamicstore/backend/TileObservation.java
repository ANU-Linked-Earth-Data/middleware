package anuled.dynamicstore.backend;

/**
 * <code>Observation</code> subclass for entire image tiles. Unlike
 * <code>PixelObservation</code>, this class uses a square grid of many pixels
 * to summarise cell contents.
 */
public final class TileObservation extends Observation {
	protected TileObservation(Cell cell, int band) {
		super(cell, band);
	}

	public int getPixelLevel() {
		int offset = (int) Math
				.round(Math.log(getCell().tileSize()) / Math.log(3));
		return getCellLevel() + offset;
	}

	/** Return contents of tile as a byte array of PNG data */
	public byte[] getTile() {
		return getCell().tileData(band);
	}

	public double getResolution() {
		return getCell().tileSize() / getCell().getDegreesSpanned();
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
