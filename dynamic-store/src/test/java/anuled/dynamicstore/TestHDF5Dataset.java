package anuled.dynamicstore;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;

public class TestHDF5Dataset {
	private HDF5Dataset ds;
	private static TestData td;
	
	@BeforeClass
	public static void setUpClass() throws IOException {
		td = new TestData();
	}
	
	@AfterClass
	public static void tearDownClass() {
		td.close();
	}
	
	@Before
	public void setUp() {
		ds = new HDF5Dataset(td.getPath());
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
		int numCells = 0;
		for (HDF5Dataset.HDF5Cell cell : ds.cells()) {
			assertNotNull(cell);
			numCells = numCells + 1;
		}
		// assertEquals(2, numCells);
	}
}
