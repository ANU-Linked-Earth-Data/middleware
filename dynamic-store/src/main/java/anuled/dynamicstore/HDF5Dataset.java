package anuled.dynamicstore;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import ch.systemsx.cisd.base.mdarray.MDShortArray;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5DoubleReader;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import ch.systemsx.cisd.hdf5.IHDF5ShortReader;
import ch.systemsx.cisd.hdf5.IHDF5StringReader;
import ncsa.hdf.hdf5lib.exceptions.HDF5SymbolTableException;

/* Class for accessing satellite observations stored using our custom HDF5 format. */

public class HDF5Dataset {
	private IHDF5Reader fp;
	private Collection<Cell> cells;

	/* Dataset-wide metadata */
	private OffsetDateTime obsDate;
	private String prodCode, satID, sensorID;

	/** Construct a new HDF5 dataset from a path to an HDF5 file */
	public HDF5Dataset(String filename) {
		fp = HDF5Factory.openForReading(filename);
		// Read all cells into core (but not their data); makes our job easier
		// later
		cells = populateCells();
		readMeta();
	}

	private void readMeta() {
		IHDF5StringReader stringReader = fp.string();
		String dateString = stringReader.getAttr("/", "datetime");
		obsDate = OffsetDateTime.parse(dateString);
		prodCode = stringReader.getAttr("/", "prod_code");
		satID = stringReader.getAttr("/", "sat_id");
		sensorID = stringReader.getAttr("/", "sensor_id");
	}

	private Collection<Cell> populateCells() {
		Queue<String> to_explore = new LinkedList<String>();
		to_explore.add("/");
		assert fp.isGroup(to_explore.peek());

		// Populate list of HDF5Cells (albeit only ones with data in them!)
		Collection<Cell> rv = new ArrayList<Cell>();
		while (!to_explore.isEmpty()) {
			String group = to_explore.remove();

			// Append this node to the index iff it looks like real data
			try {
				Cell cell = new Cell(group);
				rv.add(cell);
			} catch (Cell.NotACell e) {
				/* pass */
			}

			// Now add children to explore
			for (String child : fp.getGroupMembers(group)) {
				String childPath = group;
				if (!childPath.endsWith("/")) {
					childPath += "/";
				}
				childPath += child;
				if (fp.isGroup(childPath)) {
					to_explore.add(childPath);
				}
			}
		}
		return rv;
	}

	/** Call this function after using the class to clean up HDF5 references. */
	public void dispose() {
		fp.close();
	}

	/**
	 * Class representing a single DGGS cell, and its associated observations
	 */
	public class Cell {
		private String path, dggsIdent;
		private double[] pixelValue, centre;
		private int tileSize;

		/**
		 * Thrown by the constructor when the given cell is not in the dataset
		 */
		private class NotACell extends Exception {
			private static final long serialVersionUID = 1L;
		}

		private Cell(String path) throws NotACell {
			this.path = path;
			dggsIdent = path.replace("/", "");
			try {
				// cache the pixel value because it's (a) small enough to fit in
				// memory for every cell and (b) helps us figure out whether
				// this is a data cell or ordinary group
				pixelValue = fp.readDoubleArray(path + "/pixel");
			} catch (HDF5SymbolTableException e) {
				throw new NotACell();
			}
			long[] dims = fp.getDataSetInformation(path + "/data")
					.getDimensions();
			assert dims.length == 3 && dims[1] == dims[2]
					&& dims[0] == getNumBands();
			tileSize = (int) dims[0];

			// centre is (lon, lat)
			IHDF5DoubleReader doubleReader = fp.float64();
			centre = doubleReader.getArrayAttr(path, "centre");

			/*
			 * Other things to extract: bounds (f64x5x2), lat (f64), lon (f64),
			 * missing_value (i64).
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
			IHDF5ShortReader dataReader = fp.int16();
			MDShortArray rv = dataReader.readMDArray(path + "/data");
			return rv;
		}

		/** Return the parent dataset containing this cell */
		public HDF5Dataset getDataset() {
			return HDF5Dataset.this;
		}

		/**
		 * Get a <code>PixelObservation</code> for a single pixel covering this
		 * cell.
		 */
		public PixelObservation pixelObservation(int band) {
			return new PixelObservation(this, band);
		}

		/**
		 * Get a <code>TileObservation</code> for the data tile associated with
		 * this cell.
		 */
		public TileObservation tileObservation(int band) {
			return new TileObservation(this, band);
		}

		/**
		 * Yield a stream of pixel and tile observations across all bands of the
		 * dataset
		 */
		public Stream<Observation> observations() {
			Supplier<IntStream> mkRange = () -> {
				return IntStream.range(0, getNumBands());
			};
			return Stream.concat(mkRange.get().mapToObj(this::pixelObservation),
					mkRange.get().mapToObj(this::tileObservation));
		}

		/**
		 * Fetch the rHEALPix identifier (e.g. <code>R7852</code>) for this cell
		 */
		public String getDGGSIdent() {
			return dggsIdent;
		}

		/** Get the size of the tile associated with this cell */
		public double tileSize() {
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
	}

	/**
	 * Class representing a single "observation". An observation is tied to a
	 * single DGGS cell <em>and</em> a single band. It can be either a tile or a
	 * pixel—there are subclasses for each.
	 */
	public abstract class Observation {
		int band;
		Cell cell;

		private Observation(Cell cell, int band) {
			this.cell = cell;
			this.band = band;
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
		 * Get the level in the DGGS hierarchy that individual pixels within
		 * this observation correspond to. For single-pixel observations, this
		 * will just be the "cell level" described in
		 * <code>getCellLevel()</code>. For tiles, this will be the number of
		 * steps down the DGGS hierarchy you have to travel before a single
		 * pixel in the tile can cover a whole DGGS cell (e.g. it might be 3
		 * more than the cell level).
		 */
		public abstract int getPixelLevel();
	}

	/**
	 * <code>Observation</code> subclass for single pixel observations. In this
	 * context "pixel" means a single value summarising the contents of an
	 * entire DGGS cell (e.g. the green level averaged over the whole extent of
	 * the cell).
	 */
	public class PixelObservation extends Observation {
		private PixelObservation(Cell cell, int band) {
			super(cell, band);
		}

		public int getPixelLevel() {
			return getCellLevel();
		}

		public double getPixel() {
			return getCell().pixelData()[getBand()];
		}
	}

	/**
	 * <code>Observation</code> subclass for entire image tiles. Unlike
	 * <code>PixelObservation</code>, this class uses a square grid of many
	 * pixels to summarise cell contents.
	 */
	public class TileObservation extends Observation {
		private TileObservation(Cell cell, int band) {
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
	}

	/** Get an iterable over all cells in the dataset. */
	public Stream<Cell> cells() {
		return cells.stream();
	}

	/**
	 * Retrieve the cell corresponding to a specific DGGS ID (e.g.
	 * <code>R7852</code>). Return a <code>Cell</code> instance, or
	 * <code>null</code> if no cell can be found.
	 */
	public Cell dggsCell(String dggsID) {
		String path = dggsID.replaceAll("(.)", "/$1");
		try {
			return new Cell(path);
		} catch (Cell.NotACell e) {
			return null;
		}
	}

	// Metadata getters
	/** Retrieve the date and time at which the dataset was produced */
	public OffsetDateTime getTimestamp() {
		return obsDate;
	}

	/**
	 * Get ID of the satellite which produced the dataset (e.g.
	 * <code>"LS8"</code>)
	 */
	public String getSatID() {
		return satID;
	}

	/**
	 * Retrieve AGDC "product" (dataset) code (e.g. <code>"NBAR"</code> for some
	 * surface reflectance datasets)
	 */
	public String getProdCode() {
		return prodCode;
	}

	/**
	 * Get an identifier for the sensor which produced the dataset (e.g.
	 * <code>ETM</code> on Landsat)
	 */
	public String getSensorID() {
		return sensorID;
	}
}
