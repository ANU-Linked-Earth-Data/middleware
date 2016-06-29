package anuled.dynamicstore;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.junit.Test;

public class TestUtil {
	public static short[][] TEST_ARRAY = new short[][] {
			{ -999, 10151, 32124, -999, -999, 30839, -999, -999 },
			{ -999, 3854, 30197, -999, -999, 30839, -999, -999 },
			{ -999, -999, 19917, -999, -999, 19917, -999, -999 },
			{ 16833, -999, -999, -999, -999, -999, 9894, 9894 },
			{ 29297, 17989, 3854, -999, -999, -999, 21073, 12592 },
			{ 9894, 29940, 22487, 24928, 27241, 27755, 16062, -999 },
			{ -999, 9894, 28269, 25057, 27370, 23001, -999, -999 } };
	public static short INVALID_VALUE = -999;

	@Test
	public void testMakeImage() {
		BufferedImage im = Util.arrayToImage(TEST_ARRAY, INVALID_VALUE);
		assertEquals(7, im.getHeight());
		assertEquals(8, im.getWidth());
		// value at col 3, row 1 (i.e. (x, y) = (3, 1)) should be 0
		// (transparent)
		assertEquals(0, im.getRGB(3, 1));
		// value at col 1, row 4 (17989 short) should have intensity ~= 140
		// (ubyte) and be opaque
		int opaque = im.getRGB(1, 4);
		int expectedValue = (255 << 24) | (140 << 16) | (140 << 8) | 140;
		assertEquals(expectedValue, opaque);
	}

	@Test
	public void testImageToPNG() throws IOException {
		// Tries to convert the image to a data URL, then reconstruct it
		BufferedImage im = Util.arrayToImage(TEST_ARRAY, INVALID_VALUE);
		String b64 = Util.imageToPNGURL(im);
		String prefix = "data:image/png;base64,";
		assertTrue(b64.startsWith(prefix));
		String suffix = b64.substring(prefix.length());
		byte[] data = Base64.getDecoder().decode(suffix);
		ByteArrayInputStream inStream = new ByteArrayInputStream(data);
		BufferedImage newIm = ImageIO.read(inStream);
		assertEquals(im.getHeight(), newIm.getHeight());
		assertEquals(im.getWidth(), newIm.getWidth());
		for (int row = 0; row < im.getHeight(); row++) {
			for (int col = 0; col < im.getWidth(); col++) {
				assertEquals(im.getRGB(col, row), newIm.getRGB(col, row));
			}
		}
	}
}
