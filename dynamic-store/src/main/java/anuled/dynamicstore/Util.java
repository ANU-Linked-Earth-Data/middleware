package anuled.dynamicstore;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

/** Generic utilities that don't fit in anywhere else */
public class Util {
	public static BufferedImage arrayToImage(short[][] values, short invalidValue) {
		int width = values[0].length, height = values.length;
		// TODO: should figure out how to get a grayscale image with alpha
		BufferedImage rv = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int intensity = values[row][col];
				int r, g, b, a;
				if (intensity == invalidValue) {
					r = g = b = 0;
					a = 0;
				} else {
					int byteIntensity = Math.max(intensity, 0) / 128;
					r = g = b = byteIntensity;
					a = 255;
				}
				int rgbVal = (a << 24) | (r << 16) | (g << 8) | b;
				rv.setRGB(col, row, rgbVal);
			}
		}
		return rv;
	}
	
	public static String imageToPNGURL(BufferedImage im) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		try {
			ImageIO.write(im, "PNG", outStream);
		} catch (IOException e) {
			throw new RuntimeException("Could not convert to PNG", e);
		}
		byte[] rawData = outStream.toByteArray();
		String b64Data = Base64.getEncoder().encodeToString(rawData);
		return "data:image/png;base64," + b64Data;
	}
}
