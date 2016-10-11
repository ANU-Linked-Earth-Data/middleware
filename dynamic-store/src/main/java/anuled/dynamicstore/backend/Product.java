package anuled.dynamicstore.backend;

import ch.systemsx.cisd.hdf5.IHDF5Reader;

/**
 * Metadata class representing a particular dataset offered by the middleware
 * (e.g. <code>"landsat"</code>)
 */
public class Product {
	HDF5Dataset owner;
	String productName;
	int numBands;
	int tileSize;

	public Product(HDF5Dataset owner, String productName) {
		this.owner = owner;
		this.productName = productName;

		IHDF5Reader fp = owner.getReader();
		String prefix = "/products/" + productName;
		this.numBands = fp.readInt(prefix + "/numbands");
		this.tileSize = fp.readInt(prefix + "/tilesize");
	}

	public String getName() {
		return productName;
	}

	public int getNumBands() {
		return numBands;
	}

	public int getTileSize() {
		return tileSize;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int hashCode() {
		return productName.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Product)) {
			return false;
		}
		Product prod = (Product) other;
		return getName().equals(prod.getName());
	}
}
