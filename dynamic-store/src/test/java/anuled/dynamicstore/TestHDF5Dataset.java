package anuled.dynamicstore;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.After;

public class TestHDF5Dataset {
	HDF5Dataset ds;
	
	@Before
	public void setUp() {
		ds = new HDF5Dataset(TestData.TEST_H5_NAME);
	}
	
	@After
	public void tearDown() {
		ds.dispose();
	}
	
	@Test
	public void testInitDatset() {
		assertNotNull(ds);
	}
	
	@Test
	public void testCellIterator() {
		
	}
}
