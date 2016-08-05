package anuled.dynamicstore.backend;

/**
 * Exception which is thrown when an observation with an invalid band number is
 * created.
 */
public class InvalidBandException extends RuntimeException {

	public InvalidBandException(String message) {
		super(message);
	}

	private static final long serialVersionUID = 5323335399169380881L;

}
