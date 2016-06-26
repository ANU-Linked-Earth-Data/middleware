package anuled.dynamicstore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

/* Class for accessing satellite observations stored using our custom HDF5 format. */

public class HDF5Dataset {
	private IHDF5Reader fp;
	private Collection<HDF5Cell> cells;
	
	public HDF5Dataset(String filename) {
		fp = HDF5Factory.openForReading(filename);
		// Read all cells into core (but not their data). This makes our job a bit easier later on.
		cells = populateCells();
	}
	
	private Collection<HDF5Cell> populateCells() {
		Queue<String> to_explore = new LinkedList<String>();
		to_explore.add("/");
		assert fp.isGroup(to_explore.peek());
		
		// Populate list of HDF5Cells (albeit only ones with data in them!)
		Collection<HDF5Cell> rv = new ArrayList<HDF5Cell>();
		while (!to_explore.isEmpty()) {
			String group = to_explore.remove();
			
			// Append this node to the index iff it looks like real data
			HDF5Cell cell = new HDF5Cell(group);
			rv.add(cell);
			
			// Now add children to explore
			for (String child : fp.getGroupMembers(group)) {
				if (fp.isGroup(child)) {
					to_explore.add(child);
				}
			}
		}
		return rv;
	}
	
	public void dispose() {
		fp.close();
	}
	
	public class HDF5Cell {
		String path;
		
		private HDF5Cell(String path) {
			this.path = path;
		}
	}
	
	public Iterable<HDF5Cell> cells() {
		return new Iterable<HDF5Cell>() {
			@Override
			public Iterator<HDF5Cell> iterator() {
				return cells.iterator();
			}
		};
	}
}
