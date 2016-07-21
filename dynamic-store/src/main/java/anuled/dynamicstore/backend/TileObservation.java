package anuled.dynamicstore.backend;

import ch.systemsx.cisd.base.mdarray.MDShortArray;

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

	/** Return contents of tile in row-major order */
	public short[][] getTile() {
		MDShortArray arr = getCell().tileData();
		// XXX: This is probably _very_ slow. Might have to do some
		// profiling later to see how bad it is. It could be faster to get
		// the flat array data and reshape it somehow, but that really
		// depends on MDArray implementation details (which aren't
		// documented).
		int[] dims = arr.dimensions();
		assert dims.length == 3;
		// row major order
		short[][] rv = new short[dims[1]][dims[2]];
		for (int row = 0; row < dims[1]; row++) {
			for (int col = 0; col < dims[2]; col++) {
				rv[row][col] = arr.get(band, row, col);
			}
		}
		return rv;
	}

	public double getResolution() {
		return getCell().tileSize() / getCell().getDegreesSpanned();
	}
}
