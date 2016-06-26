package anuled.dynamicstore;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;

public class TestHDF5Dataset {
	HDF5Dataset ds;
	static File tempFile;
	
	@BeforeClass
	public static void setUpClass() throws IOException {
		Class<? extends TestHDF5Dataset> cls = TestHDF5Dataset.class;
		InputStream readStream = cls.getResourceAsStream(TestData.TEST_H5_NAME);
		try {
			String baseName = new File(TestData.TEST_H5_NAME).getName();
			tempFile = File.createTempFile(baseName, "");
			tempFile.deleteOnExit(); // defensive; we delete on tearDown anyway

			FileOutputStream outStream = new FileOutputStream(tempFile);
			try {
				byte[] buffer = new byte[8192];
				while (true) {
					int numRead = readStream.read(buffer);
					if (numRead <= 0) {
						break;
					}
					byte[] writeBuffer = Arrays.copyOfRange(buffer, 0, numRead);
					outStream.write(writeBuffer);
				}
			} finally {
				outStream.close();
			}
		} finally {
			readStream.close();
		}
	}
	
	@Before
	public void setUp() {
		ds = new HDF5Dataset(tempFile.getAbsolutePath());
	}
	
	@After
	public void tearDown() {
		ds.dispose();
	}
	
	@AfterClass
	public static void tearDownClass() {
		tempFile.delete();
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
