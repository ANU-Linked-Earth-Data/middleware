package anuled.dynamicstore;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestHDF5Dataset {
	HDF5Dataset ds;
	
	@Before
	public void setUp() {
		ds = new HDF5Dataset(TestData.TEST_H5_NAME);
	}
	
	@Test
	public void testInitDatset() {
		assertTrue(true);
	}
}
