package exception;

/**
 * Thrown when the user types invalid or empty data in a form.
 * Checked exception: the compiler forces the caller to handle it.
 */
public class InvalidInputException extends Exception {

    public InvalidInputException(String message) {
        super(message);
    }
}
