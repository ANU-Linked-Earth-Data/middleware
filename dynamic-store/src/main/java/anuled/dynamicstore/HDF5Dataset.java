package anuled.dynamicstore;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

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

	public void dispose() {
		fp.close();
	}

	public class Cell {
		private String path, dggsIdent;
		private double[] pixelValue, centre;
		private int tileSize;

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
			assert dims.length == 3 && dims[0] == dims[1]
					&& dims[3] == getNumBands();
			tileSize = (int) dims[0];

			IHDF5DoubleReader doubleReader = fp.float64();
			centre = doubleReader.getArrayAttr(path, "centre");

			/*
			 * Other things to extract: bounds (f64x5x2), lat (f64), lon (f64),
			 * missing_value (i64).
			 */
		}

		public int getNumBands() {
			return pixelValue.length;
		}

		public double[] getPixel() {
			return pixelValue;
		}

		public MDShortArray getData() {
			IHDF5ShortReader dataReader = fp.int16();
			MDShortArray rv = dataReader.readMDArray(path + "/data");
			return rv;
		}

		public HDF5Dataset getDataset() {
			return HDF5Dataset.this;
		}

		public String getDGGSIdent() {
			return dggsIdent;
		}

		public double tileSize() {
			return tileSize;
		}

		public double getLat() {
			return centre[0];
		}

		public double getLon() {
			return centre[1];
		}
	}

	public abstract class Observation {
		int band;
		Cell cell;

		private Observation(Cell cell, int band) {
			this.cell = cell;
			this.band = band;
		}

		public Cell getCell() {
			return cell;
		}

		public int getBand() {
			return band;
		}

		public int getLevelSquare() {
			return cell.getDGGSIdent().length();
		}

		public abstract int getLevelPixel();
	}

	public class PixelObservation extends Observation {
		public PixelObservation(Cell cell, int band) {
			super(cell, band);
		}

		public int getLevelPixel() {
			return getLevelSquare();
		}
	}

	public class TileObservation extends Observation {
		public TileObservation(Cell cell, int band) {
			super(cell, band);
		}

		public int getLevelPixel() {
			return (int) Math.round(Math.log(cell.tileSize()) / Math.log(3));
		}
	}

	public Iterable<Cell> cells() {
		return new Iterable<Cell>() {
			@Override
			public Iterator<Cell> iterator() {
				return cells.iterator();
			}
		};
	}

	// Metadata getters
	public OffsetDateTime getTimestamp() {
		return obsDate;
	}

	public String getSatID() {
		return satID;
	}

	public String getProdCode() {
		return prodCode;
	}

	public String getSensorID() {
		return sensorID;
	}
}
