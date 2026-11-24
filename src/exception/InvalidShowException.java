package exception;

/**
 * Thrown when a show does not exist or can no longer be booked.
 * Checked exception: the compiler forces the caller to handle it.
 */
public class InvalidShowException extends Exception {

    public InvalidShowException(String message) {
        super(message);
    }
}
