package anuled.dynamicstore;

/* Class for accessing satellite observations stored using our custom HDF5 format. */

import ncsa.hdf.object.h5.H5File;

public class HDF5Dataset {
	public HDF5Dataset(String filename) {
		H5File fp = new H5File(filename, H5File.READ);
	}
}
