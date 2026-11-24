package exception;

/**
 * Thrown when a booking cannot be cancelled (already cancelled, too late, not your booking).
 * Checked exception: the compiler forces the caller to handle it.
 */
public class BookingCancellationException extends Exception {

    public BookingCancellationException(String message) {
        super(message);
    }
}
