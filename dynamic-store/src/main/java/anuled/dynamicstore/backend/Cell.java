package anuled.dynamicstore.backend;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import ch.systemsx.cisd.hdf5.IHDF5ByteReader;
import ch.systemsx.cisd.hdf5.IHDF5DoubleReader;
import ch.systemsx.cisd.hdf5.IHDF5IntReader;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import ch.systemsx.cisd.hdf5.IHDF5ShortReader;
import ncsa.hdf.hdf5lib.exceptions.HDF5SymbolTableException;

/**
 * Class representing a single DGGS cell, and its associated observations
 */
public class Cell {
	private String path, dggsIdent;
	private double[] centre;
	private List<List<Double>> bounds;
	private double latMin, latMax, longMin, longMax;
	private double degreesSpanned;
	HDF5Dataset owner;
	private Map<Product, Set<ZonedDateTime>> availableProducts;

	/**
	 * Thrown by the constructor when the given cell is not in the dataset
	 */
	protected class NotACell extends Exception {
		private static final long serialVersionUID = 1L;
	}

	protected Cell(String path, HDF5Dataset owner) throws NotACell {
		this.owner = owner;
		this.path = path;

		IHDF5Reader fp = owner.getReader();

		dggsIdent = path.replace("/", "");
		for (Product prod : owner.getProducts()) {
			String groupPath = path + "/" + prod.getName();
			if (fp.isGroup(groupPath)) {
				for (String member : fp.getGroupMembers(groupPath)) {
					if (member.startsWith("pixel@")) {
						String[] timeStrings = member.split("@", 1);
						availableProducts
								.computeIfAbsent(prod,
										k -> new HashSet<ZonedDateTime>())
								.add(ZonedDateTime.parse(timeStrings[1]));
					}
				}
			}
		}

		// centre is (lon, lat)
		IHDF5DoubleReader doubleReader = fp.float64();
		centre = doubleReader.getArrayAttr(path, "centre");
		// bounds are list of (lon, lat), IIRC
		double[][] allBounds = doubleReader.getMatrixAttr(path, "bounds");
		// We need at least four coordinates to make a non-degenerate shape
		assert allBounds.length >= 4;
		bounds = new ArrayList<List<Double>>();
		latMin = longMin = Double.POSITIVE_INFINITY;
		latMax = longMax = Double.NEGATIVE_INFINITY;
		for (int row = 0; row < allBounds.length; row++) {
			List<Double> pair = new ArrayList<Double>(2);
			double[] thisRow = allBounds[row];
			assert thisRow.length == 2;
			double lon = thisRow[0];
			double lat = thisRow[1];
			pair.add(lon);
			pair.add(lat);
			bounds.add(pair);

			latMin = Math.min(latMin, lat);
			latMax = Math.max(latMax, lat);
			longMin = Math.min(longMin, lon);
			longMax = Math.max(longMax, lon);
		}

		// This will be used for resolution calculation
		degreesSpanned = (latMax - latMin + longMax - longMin) / 2;
	}

	protected IHDF5Reader getReader() {
		return owner.getReader();
	}

	protected String getPath() {
		return path;
	}

	/**
	 * Approximate number of degrees (of latitude or longitude) spanned by the
	 * cell
	 */
	public double getDegreesSpanned() {
		return degreesSpanned;
	}

	/**
	 * Does this cell fall entirely within the rectangle defined by the given
	 * latitude and longitude bounds?
	 */
	public boolean inRect(Double lonMin, Double lonMax, Double latMin,
			Double latMax) {
		return (lonMin == null || this.longMin >= lonMin)
				&& (lonMax == null || this.longMax <= lonMax)
				&& (latMin == null || this.latMin >= latMin)
				&& (latMax == null || this.latMax <= latMax);
	}

	/** Return the parent dataset containing this cell */
	public HDF5Dataset getDataset() {
		return owner;
	}

	/**
	 * Get a <code>PixelObservation</code> for a single pixel covering this
	 * cell.
	 */
	public PixelObservation pixelObservation(Product product,
			ZonedDateTime timestamp, int band) {
		return new PixelObservation(this, product, timestamp, band);
	}

	/**
	 * Get a <code>TileObservation</code> for the data tile associated with this
	 * cell.
	 */
	public TileObservation tileObservation(Product product,
			ZonedDateTime timestamp, int band) {
		return new TileObservation(this, product, timestamp, band);
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
		// TODO: I think I can use a stream mapping over availableProducts here.
		// The rest of the logic will have to be moved inside the mapping
		// function.
		Supplier<IntStream> mkRange;
		if (band == null) {
			mkRange = () -> {
				return IntStream.range(0, getNumBands());
			};
		} else if (band < 0 || band >= getNumBands()) {
			// can only get valid bands
			return Stream.of();
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

	/** Get the latitude of the centre of the cell */
	public double getLat() {
		return centre[1];
	}

	/** Get the longitude of the centre of the cell */
	public double getLon() {
		return centre[0];
	}

	public double getLatMin() {
		return latMin;
	}

	public double getLatMax() {
		return latMax;
	}

	public double getLongMin() {
		return longMin;
	}

	public double getLongMax() {
		return longMax;
	}

	@Override
	public String toString() {
		return "Cell " + getDGGSIdent();
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
			return ((Cell) other).getDGGSIdent().equals(getDGGSIdent());
		}
		return false;
	}
}
