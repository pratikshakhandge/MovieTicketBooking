package exception;

/**
 * Thrown when a show is completely full.
 * Checked exception: the compiler forces the caller to handle it.
 */
public class NoSeatsAvailableException extends Exception {

    public NoSeatsAvailableException(String message) {
        super(message);
    }
}
