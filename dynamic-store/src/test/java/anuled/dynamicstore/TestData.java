package anuled.dynamicstore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/** Utility class which exposes a test HDF5 file for unit tests to use. */
public final class TestData {
	private static String TEST_H5_NAME = "/LS8_OLI_TIRS_NBAR_149_-036_2013-05-27T23-58-20.h5";
	private File tempFile;

	/**
	 * Construct a new test HDF5 file by creating a node in <code>/tmp</code>
	 * and filling it with the contents of a HDF5 file stored in the test
	 * resources.
	 */
	public TestData() throws IOException {
		Class<? extends TestHDF5Dataset> cls = TestHDF5Dataset.class;
		InputStream readStream = cls.getResourceAsStream(TestData.TEST_H5_NAME);
		try {
			String baseName = new File(TestData.TEST_H5_NAME).getName();
			tempFile = File.createTempFile(baseName, null);
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

	/** Get the path to the temporary file created by the constructor */
	public String getPath() {
		return tempFile.getAbsolutePath();
	}

	/** Clean up all resources created by the constructor */
	public void dispose() {
		tempFile.delete();
		tempFile = null;
	}
}
