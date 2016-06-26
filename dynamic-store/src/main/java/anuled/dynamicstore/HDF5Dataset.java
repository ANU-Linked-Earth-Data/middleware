package anuled.dynamicstore;

import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;

/* Class for accessing satellite observations stored using our custom HDF5 format. */

import ncsa.hdf.object.h5.H5File;

public class HDF5Dataset {
	private H5File fp;
	
	public HDF5Dataset(String filename) {
		fp = new H5File(filename, H5File.READ);
	}
	
	public void dispose() {
		try {
			fp.close();
		} catch (HDF5Exception e) {
			throw new RuntimeException("Could not close file (see inner)", e);
		}
	}
	
	public Iterable<HDF5Cell> cells() {
		return null;
	}
}
