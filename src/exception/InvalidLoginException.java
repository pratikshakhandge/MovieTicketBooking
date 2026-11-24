package exception;

/**
 * Thrown when the email or password is wrong.
 * Checked exception: the compiler forces the caller to handle it.
 */
public class InvalidLoginException extends Exception {

    public InvalidLoginException(String message) {
        super(message);
    }
}
