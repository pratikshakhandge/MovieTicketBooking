package exception;

/**
 * Thrown when a selected seat has already been booked (prevents double booking).
 * Checked exception: the compiler forces the caller to handle it.
 */
public class SeatAlreadyBookedException extends Exception {

    public SeatAlreadyBookedException(String message) {
        super(message);
    }
}
