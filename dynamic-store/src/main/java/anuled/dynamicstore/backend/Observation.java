package anuled.dynamicstore.backend;

import java.time.ZonedDateTime;

/**
 * Class representing a single "observation". An observation is tied to a single
 * DGGS cell <em>and</em> a single band. It can be either a tile or a
 * pixel—there are subclasses for each.
 */
public abstract class Observation {
	int band;
	Cell cell;
	String product;
	ZonedDateTime timestamp;

	protected Observation(Cell cell, String product, ZonedDateTime timestamp,
			int band) {
		if (band < 0 || band >= cell.getNumBands()) {
			throw new InvalidBandException("Band " + band + " out of range [0, "
					+ cell.getNumBands() + ")");
		}
		this.cell = cell;
		this.band = band;
		this.product = product;
		this.timestamp = timestamp;
	}

	/** DGGS cell associated with this observation */
	public Cell getCell() {
		return cell;
	}

	/** Sensor band associated with this observation. */
	public int getBand() {
		return band;
	}

	/**
	 * Get the level in the DGGS hierarchy of the cell corresponding to this
	 * observation.
	 */
	public int getCellLevel() {
		return getCell().getDGGSIdent().length();
	}

	/**
	 * Fetch the name of the AGDC product which this observation corresponds to
	 * (e.g. LS8_OLI_NBAR for some Landsat 8 data)
	 */
	public String getProduct() {
		return product;
	}

	/**
	 * Get the time at which this observation was produced.
	 */
	public ZonedDateTime getTimestamp() {
		return timestamp;
	}

	/**
	 * Get the level in the DGGS hierarchy that individual pixels within this
	 * observation correspond to. For single-pixel observations, this will just
	 * be the "cell level" described in <code>getCellLevel()</code>. For tiles,
	 * this will be the number of steps down the DGGS hierarchy you have to
	 * travel before a single pixel in the tile can cover a whole DGGS cell
	 * (e.g. it might be 3 more than the cell level).
	 */
	public abstract int getPixelLevel();

	public abstract double getResolution();

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Observation)) {
			return false;
		}
		Observation obsOther = (Observation) other;
		return getCell().equals(obsOther.getCell())
				&& getBand() == obsOther.getBand();
	}

	@Override
	public int hashCode() {
		return getBand() ^ getCell().hashCode();
	}

	@Override
	public String toString() {
		return "Observation: band=" + getBand() + ", cell="
				+ getCell().getDGGSIdent();
	}
}
