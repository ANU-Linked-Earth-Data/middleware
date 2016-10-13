package anuled.dynamicstore.backend;

import java.io.StringReader;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import ch.systemsx.cisd.hdf5.IHDF5Reader;

/**
 * Metadata class representing a particular dataset offered by the middleware
 * (e.g. <code>"landsat"</code>)
 */
public class Product {
	HDF5Dataset owner;
	String productName;
	int numBands, tileSize;
	Model metaModel;

	public Product(HDF5Dataset owner, String productName) {
		this.owner = owner;
		this.productName = productName;

		// Read basic metadata (number of bands, size of tiles in DGGS cells)
		IHDF5Reader fp = owner.getReader();
		String prefix = "/products/" + productName;
		this.numBands = fp.readInt(prefix + "/numbands");
		this.tileSize = fp.readInt(prefix + "/tilesize");

		// Read and parse Turtle-formatted metadata
		String metaString = fp.readString(prefix + "/meta");
		StringReader metaReader = new StringReader(metaString);
		metaModel = ModelFactory.createDefaultModel();
		metaModel.read(metaReader, "", "TTL");
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

	// Get a placeholder resource representing extra attributes for the
	// qb:DataSet corresponding to this product.
	public Resource getQBDSAttributes() {
		return metaModel.getResource("#extraAttributes");
	}

	// Return a model of everything that's not the fake qb:DataStructure
	// returned by getQBDSAttributes
	public Model getFreeFormMetadata() {
		Model rv = ModelFactory.createDefaultModel();
		rv.add(metaModel);
		rv.removeAll(getQBDSAttributes(), null, null);
		return rv;
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
