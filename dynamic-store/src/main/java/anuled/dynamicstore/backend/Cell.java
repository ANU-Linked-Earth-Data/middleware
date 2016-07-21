package anuled.dynamicstore.backend;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import ch.systemsx.cisd.base.mdarray.MDShortArray;
import ch.systemsx.cisd.hdf5.IHDF5DoubleReader;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import ch.systemsx.cisd.hdf5.IHDF5ShortReader;
import ncsa.hdf.hdf5lib.exceptions.HDF5SymbolTableException;

/**
 * Class representing a single DGGS cell, and its associated observations
 */
public class Cell {
	private String path, dggsIdent;
	private double[] pixelValue, centre;
	private int tileSize;
	private short invalidValue;
	private List<List<Double>> bounds;
	private double degreesSpanned;
	HDF5Dataset owner;

	/**
	 * Thrown by the constructor when the given cell is not in the dataset
	 */
	protected class NotACell extends Exception {
		private static final long serialVersionUID = 1L;
	}

	protected Cell(String path, HDF5Dataset owner) throws NotACell {
		this.owner = owner;
		this.path = path;

		// TODO: Write all the required data reading methods into
		// HDF5Dataset.java so that I don't need to deal with HDF5 stuff in this
		// class
		IHDF5Reader fp = owner.getReader();

		dggsIdent = path.replace("/", "");
		try {
			// cache the pixel value because it's (a) small enough to fit in
			// memory for every cell and (b) helps us figure out whether
			// this is a data cell or ordinary group
			pixelValue = fp.readDoubleArray(path + "/pixel");
		} catch (HDF5SymbolTableException e) {
			throw new NotACell();
		}
		long[] dims = fp.getDataSetInformation(path + "/data").getDimensions();
		assert dims.length == 3 && dims[1] == dims[2]
				&& dims[0] == getNumBands();
		tileSize = (int) dims[1];

		// centre is (lon, lat)
		IHDF5DoubleReader doubleReader = fp.float64();
		centre = doubleReader.getArrayAttr(path, "centre");

		IHDF5ShortReader shortReader = fp.int16();
		invalidValue = shortReader.getAttr(path, "missing_value");

		// bounds are list of (lon, lat), IIRC
		double[][] allBounds = doubleReader.getMatrixAttr(path, "bounds");
		// We need at least four coordinates to make a non-degenerate shape
		assert allBounds.length >= 4;
		bounds = new ArrayList<List<Double>>();
		double minLat, maxLat, minLon, maxLon;
		minLat = minLon = Double.POSITIVE_INFINITY;
		maxLat = maxLon = Double.NEGATIVE_INFINITY;
		for (int row = 0; row < allBounds.length; row++) {
			List<Double> pair = new ArrayList<Double>(2);
			double[] thisRow = allBounds[row];
			assert thisRow.length == 2;
			double lon = thisRow[0];
			double lat = thisRow[1];
			pair.add(lon);
			pair.add(lat);
			bounds.add(pair);

			minLat = Math.min(minLat, lat);
			maxLat = Math.max(maxLat, lat);
			minLon = Math.min(minLon, lon);
			maxLon = Math.max(maxLon, lon);
		}

		// This will be used for resolution calculation
		degreesSpanned = (maxLat - minLat + maxLon - minLon) / 2;

		/*
		 * Other attributes to extract (if needed): lat (f64), lon (f64)
		 */
	}

	/** Get the number of bands in observations associated with this cell */
	public int getNumBands() {
		return pixelValue.length;
	}

	/** Get all channels of a pixel covering this cell */
	public double[] pixelData() {
		// TODO: Rethink this API. Should pixelData and tileData be moved
		// into the *Observation classes?
		return pixelValue;
	}

	/** Get all channels and pixels of a tile covering this cell */
	public MDShortArray tileData() {
		IHDF5Reader fp = owner.getReader();
		IHDF5ShortReader dataReader = fp.int16();
		MDShortArray rv = dataReader.readMDArray(path + "/data");
		return rv;
	}

	/**
	 * Approximate number of degrees (of latitude or longitude) spanned by the
	 * cell
	 */
	public double getDegreesSpanned() {
		return degreesSpanned;
	}

	/** Return the parent dataset containing this cell */
	public HDF5Dataset getDataset() {
		return owner;
	}

	/**
	 * Get a <code>PixelObservation</code> for a single pixel covering this
	 * cell.
	 */
	public PixelObservation pixelObservation(int band) {
		return new PixelObservation(this, band);
	}

	/**
	 * Get a <code>TileObservation</code> for the data tile associated with this
	 * cell.
	 */
	public TileObservation tileObservation(int band) {
		return new TileObservation(this, band);
	}

	/**
	 * Yield a stream of pixel and tile observations across all bands of the
	 * dataset
	 * 
	 * @param band
	 *            sensor band for all observations, or null if all bands are
	 *            desired.
	 * @param expectedType
	 *            {@link TileObservation}, {@link PixelObservation} or null,
	 *            depending on which type of observation (if any particular
	 *            type) is desired.
	 */
	public Stream<Observation> observations(Integer band,
			Class<?> expectedType) {
		Supplier<IntStream> mkRange;
		if (band == null) {
			mkRange = () -> {
				return IntStream.range(0, getNumBands());
			};
		} else {
			mkRange = () -> {
				return IntStream.of(band);
			};
		}

		boolean expectsPixel;
		if (expectedType == null) {
			expectsPixel = false; // arbitrary, fixes linter warnings
		} else if (expectedType.equals(PixelObservation.class)) {
			expectsPixel = true;
		} else if (expectedType.equals(TileObservation.class)) {
			expectsPixel = false;
		} else {
			throw new RuntimeException("Expected type must be "
					+ "PixelObservation or TileObservation");
		}

		// Now construct the return stream
		Stream<Observation> rvStream = Stream.of();
		if (expectedType == null || !expectsPixel) {
			rvStream = Stream.concat(rvStream,
					mkRange.get().mapToObj(this::tileObservation));
		}
		if (expectedType == null || expectsPixel) {
			rvStream = Stream.concat(rvStream,
					mkRange.get().mapToObj(this::pixelObservation));
		}
		return rvStream;
	}

	/**
	 * Fetch the rHEALPix identifier (e.g. <code>R7852</code>) for this cell
	 */
	public String getDGGSIdent() {
		return dggsIdent;
	}

	/** Get the size of the tile associated with this cell */
	public int tileSize() {
		return tileSize;
	}

	/** Get the latitude of the centre of the cell */
	public double getLat() {
		return centre[1];
	}

	/** Get the longitude of the centre of the cell */
	public double getLon() {
		return centre[0];
	}

	@Override
	public String toString() {
		return "Cell " + getDGGSIdent();
	}

	/** Returns the int16 value used to mark invalid pixels */
	public short getInvalidValue() {
		return invalidValue;
	}

	public List<List<Double>> getBounds() {
		return bounds;
	}

	@Override
	public int hashCode() {
		return dggsIdent.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Cell) {
			return ((Cell) other).getDGGSIdent() == dggsIdent;
		}
		return false;
	}
}
