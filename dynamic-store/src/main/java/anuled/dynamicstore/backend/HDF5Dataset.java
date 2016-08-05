package anuled.dynamicstore.backend;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Stream;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import ch.systemsx.cisd.hdf5.IHDF5StringReader;

/* Class for accessing satellite observations stored using our custom HDF5 format. */

public class HDF5Dataset {
	private IHDF5Reader fp;
	private Map<Integer, Collection<Cell>> cellsByLevel;
	private Map<String, Cell> cellsByID;

	/* Dataset-wide metadata */
	private OffsetDateTime obsDate;
	private String prodCode, satID, sensorID;

	protected IHDF5Reader getReader() {
		return fp;
	}

	/** Construct a new HDF5 dataset from a path to an HDF5 file */
	public HDF5Dataset(String filename) {
		fp = HDF5Factory.openForReading(filename);
		// Read all cells into core (but not their data); makes our job easier
		// later
		populateCells();
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

	private void populateCells() {
		cellsByLevel = new HashMap<Integer, Collection<Cell>>();
		cellsByID = new HashMap<String, Cell>();
		Queue<String> to_explore = new LinkedList<String>();
		to_explore.add("/");
		assert fp.isGroup(to_explore.peek());

		// Populate list of HDF5Cells (albeit only ones with data in them!)
		while (!to_explore.isEmpty()) {
			String group = to_explore.remove();

			// Append this node to the index iff it looks like real data
			try {
				Cell cell = new Cell(group, this);
				Integer level = cell.getDGGSIdent().length();
				if (cellsByLevel.containsKey(level)) {
					cellsByLevel.get(level).add(cell);
				} else {
					Collection<Cell> newList = new ArrayList<Cell>();
					newList.add(cell);
					cellsByLevel.put(Integer.valueOf(level), newList);
				}
				cellsByID.put(cell.getDGGSIdent(), cell);
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
	}

	/** Call this function after using the class to clean up HDF5 references. */
	public void dispose() {
		fp.close();
	}

	/**
	 * Get an iterable over cells in the dataset.
	 * 
	 * @param cellLevel
	 *            numeric level in the DGGS hierarchy at which the cell should
	 *            occur, or null if all levels desired.
	 * @param cellID
	 *            DGGS ID for the cell (e.g. R78523).
	 * @throws Exception
	 */
	public Stream<Cell> cells(Integer cellLevel, String cellID) {
		// If a cell ID was specified, use it
		if (cellID != null) {
			// Having the wrong cell level makes us return nothing
			if (cellLevel == null || cellLevel.equals(cellID.length())) {
				Cell theCell = cellsByID.get(cellID);
				if (theCell != null) {
					return Stream.of(theCell);
				}
			}
			return Stream.of();
		}

		// If a cell level was specified, but not a cell ID, return all cells at
		// the given level
		if (cellLevel != null) {
			Collection<Cell> theCells = cellsByLevel.get(cellLevel);
			if (theCells != null) {
				return theCells.stream();
			}
			return Stream.of();
		}

		// If no constraints were specified, return all cells
		return cellsByLevel.values().stream().flatMap(l -> l.stream());
	}

	/**
	 * Retrieve the cell corresponding to a specific DGGS ID (e.g.
	 * <code>R7852</code>).
	 * 
	 * @return <code>Cell</code> instance, or <code>null</code> if no cell can
	 *         be found.
	 */
	public Cell dggsCell(String dggsID) {
		String path = dggsID.replaceAll("(.)", "/$1");
		try {
			return new Cell(path, this);
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
