package anuled.dynamicstore.util;

public class DGGSUtil {
	// Check whether a given character is a valid part of a rHEALPix identifier
	public static boolean isCellChar(char cellChar) {
		switch (cellChar) {
		case 'A':
		case 'B':
		case 'C':
		case 'D':
		case 'E':
		case 'F':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			return true;
		}
		return false;
	}
}
