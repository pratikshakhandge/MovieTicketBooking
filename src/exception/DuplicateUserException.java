package exception;

/**
 * Thrown when someone registers with an email that already exists.
 * Checked exception: the compiler forces the caller to handle it.
 */
public class DuplicateUserException extends Exception {

    public DuplicateUserException(String message) {
        super(message);
    }
}
