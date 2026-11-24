package service;

import dao.BookingDAO;
import dao.SeatDAO;
import dao.ShowDAO;
import exception.BookingCancellationException;
import exception.DatabaseException;
import exception.InvalidInputException;
import exception.InvalidShowException;
import exception.NoSeatsAvailableException;
import exception.SeatAlreadyBookedException;
import model.Booking;
import model.Show;
import model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Business rules for booking tickets, cancelling and booking history. */
public class BookingService {

    /** Rule 1: at most this many seats in one booking. */
    public static final int MAX_SEATS_PER_BOOKING = 6;
    /** Rule 2: a booking can be cancelled only if the show starts in more than this many hours. */
    public static final int CANCEL_BEFORE_HOURS = 2;

    private final BookingDAO bookingDAO = new BookingDAO();
    private final ShowDAO showDAO = new ShowDAO();
    private final SeatDAO seatDAO = new SeatDAO();

    /** Ticket price = price of one ticket x number of seats. */
    public static double calculateTotal(double ticketPrice, int seatCount) {
        return ticketPrice * seatCount;
    }

    /** Validates the request and books the seats. Returns the saved booking for the confirmation screen. */
    public Booking bookSeats(User user, int showId, List<String> seatNumbers)
            throws InvalidInputException, InvalidShowException, NoSeatsAvailableException,
                   SeatAlreadyBookedException, DatabaseException {

        if (seatNumbers == null || seatNumbers.isEmpty()) {
            throw new InvalidInputException("Please select at least one seat.");
        }
        if (seatNumbers.size() > MAX_SEATS_PER_BOOKING) {
            throw new InvalidInputException("You can book at most " + MAX_SEATS_PER_BOOKING + " seats at a time.");
        }

        Show show = showDAO.getShowById(showId);
        if (show == null) {
            throw new InvalidShowException("Show with ID " + showId + " does not exist.");
        }
        if (!show.getShowDateTime().isAfter(LocalDateTime.now())) {
            throw new InvalidShowException("This show has already started. Booking is closed.");
        }
        if (seatDAO.countAvailableSeats(showId) == 0) {
            throw new NoSeatsAvailableException("Sorry, this show is house full. No seats are available.");
        }

        // sorted order avoids database deadlocks when two customers book at the same time
        List<String> sortedSeats = new ArrayList<>(seatNumbers);
        Collections.sort(sortedSeats);

        int bookingId = bookingDAO.createBooking(user.getUserId(), showId, sortedSeats);
        return bookingDAO.getBookingById(bookingId);
    }

    public List<Booking> getBookingsForUser(int userId) throws DatabaseException {
        return bookingDAO.getBookingsByUser(userId);
    }

    public List<Booking> getAllBookings() throws DatabaseException {
        return bookingDAO.getAllBookings();
    }

    /** Cancels a booking if the cancellation rules allow it. */
    public void cancelBooking(User user, int bookingId) throws BookingCancellationException, DatabaseException {
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null) {
            throw new BookingCancellationException("Booking " + bookingId + " does not exist.");
        }
        if (booking.getUserId() != user.getUserId()) {
            throw new BookingCancellationException("You can cancel only your own bookings.");
        }
        if (!booking.isConfirmed()) {
            throw new BookingCancellationException("This booking is already cancelled.");
        }
        LocalDateTime lastCancelTime = booking.getShowDateTime().minusHours(CANCEL_BEFORE_HOURS);
        if (!LocalDateTime.now().isBefore(lastCancelTime)) {
            throw new BookingCancellationException("Cancellation is allowed only until " + CANCEL_BEFORE_HOURS
                    + " hours before the show. This show starts too soon.");
        }
        bookingDAO.cancelBooking(bookingId);
    }

    /** Confirmed bookings of this user whose show starts within the next 'hoursAhead' hours (Streams). */
    public List<Booking> getUpcomingBookings(int userId, int hoursAhead) throws DatabaseException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = now.plusHours(hoursAhead);
        return bookingDAO.getBookingsByUser(userId).stream()
                .filter(Booking::isConfirmed)
                .filter(b -> b.getShowDateTime().isAfter(now) && b.getShowDateTime().isBefore(limit))
                .sorted(Comparator.comparing(Booking::getShowDateTime))
                .collect(Collectors.toList());
    }
}
