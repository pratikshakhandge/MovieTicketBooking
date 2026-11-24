package exception;

/**
 * Our own exception for database problems.
 * Extends Exception, so callers MUST handle it (a "checked" exception).
 */
public class DatabaseException extends Exception {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
