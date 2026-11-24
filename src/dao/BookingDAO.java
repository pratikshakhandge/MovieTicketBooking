package dao;

import database.DBConnection;
import exception.BookingCancellationException;
import exception.DatabaseException;
import exception.InvalidInputException;
import exception.InvalidShowException;
import exception.SeatAlreadyBookedException;
import model.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * All SQL related to bookings and booking_seats.
 * Booking and cancelling use TRANSACTIONS (commit / rollback).
 */
public class BookingDAO {

    /** One booking per row, with customer, movie, show and seat details joined in. */
    private static final String SELECT_BOOKINGS =
            "SELECT b.booking_id, b.user_id, u.full_name, b.show_id, m.title, s.show_date, s.show_time, "
                    + "s.screen_name, b.booking_date, b.total_amount, b.status, "
                    + "COUNT(bs.seat_id) AS seat_count, "
                    + "COALESCE(GROUP_CONCAT(se.seat_number ORDER BY se.seat_number SEPARATOR ', '), '') AS seat_numbers "
                    + "FROM bookings b "
                    + "JOIN users u ON b.user_id = u.user_id "
                    + "JOIN shows s ON b.show_id = s.show_id "
                    + "JOIN movies m ON s.movie_id = m.movie_id "
                    + "LEFT JOIN booking_seats bs ON b.booking_id = bs.booking_id "
                    + "LEFT JOIN seats se ON bs.seat_id = se.seat_id ";

    private static final String GROUP_AND_ORDER =
            " GROUP BY b.booking_id, b.user_id, u.full_name, b.show_id, m.title, s.show_date, s.show_time, "
                    + "s.screen_name, b.booking_date, b.total_amount, b.status "
                    + "ORDER BY b.booking_id DESC";

    // =====================================================================
    //  BOOK SEATS (transaction)
    // =====================================================================

    /**
     * Books the given seats for a user in ONE transaction.
     * Steps: 1) read price  2) mark every seat as booked  3) create the booking
     *        4) link booking and seats  5) commit.
     * If anything fails, everything is rolled back and nothing is saved.
     *
     * @return the new booking_id
     */
    public int createBooking(int userId, int showId, List<String> seatNumbers)
            throws DatabaseException, SeatAlreadyBookedException, InvalidShowException, InvalidInputException {

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);                          // START TRANSACTION

            // 1) price of one ticket (also checks that the show exists)
            double price = 0;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT ticket_price FROM shows WHERE show_id = ?")) {
                ps.setInt(1, showId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new InvalidShowException("Show with ID " + showId + " does not exist.");
                    }
                    price = rs.getDouble(1);
                }
            }

            // 2) mark each seat as booked; the "AND is_booked = FALSE" condition stops double booking
            List<Integer> seatIds = new ArrayList<>();
            for (String seatNumber : seatNumbers) {
                int seatId = findSeatId(con, showId, seatNumber);
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE seats SET is_booked = TRUE WHERE seat_id = ? AND is_booked = FALSE")) {
                    ps.setInt(1, seatId);
                    if (ps.executeUpdate() == 0) {
                        throw new SeatAlreadyBookedException(
                                "Seat " + seatNumber + " was just booked by someone else. Please choose another seat.");
                    }
                }
                seatIds.add(seatId);
            }

            // 3) create the booking row
            double totalAmount = price * seatNumbers.size();
            int bookingId;
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO bookings (user_id, show_id, total_amount, status) VALUES (?, ?, ?, 'CONFIRMED')",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setInt(2, showId);
                ps.setDouble(3, totalAmount);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No booking ID was returned.");
                    }
                    bookingId = keys.getInt(1);
                }
            }

            // 4) link the booking with its seats
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO booking_seats (booking_id, seat_id) VALUES (?, ?)")) {
                for (int seatId : seatIds) {
                    ps.setInt(1, bookingId);
                    ps.setInt(2, seatId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            con.commit();                                      // 5) COMMIT: save everything
            return bookingId;

        } catch (SQLException e) {
            rollback(con);                                     // ROLLBACK: undo everything
            throw new DatabaseException("Booking failed and was cancelled safely: " + e.getMessage(), e);
        } catch (SeatAlreadyBookedException | InvalidShowException | InvalidInputException e) {
            rollback(con);
            throw e;
        } finally {
            close(con);
        }
    }

    // =====================================================================
    //  CANCEL BOOKING (transaction)
    // =====================================================================

    /** Marks the booking as CANCELLED and frees its seats, in one transaction. */
    public void cancelBooking(int bookingId) throws DatabaseException, BookingCancellationException {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE bookings SET status = 'CANCELLED' WHERE booking_id = ? AND status = 'CONFIRMED'")) {
                ps.setInt(1, bookingId);
                if (ps.executeUpdate() == 0) {
                    throw new BookingCancellationException(
                            "Booking " + bookingId + " does not exist or is already cancelled.");
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE seats SET is_booked = FALSE WHERE seat_id IN "
                            + "(SELECT seat_id FROM booking_seats WHERE booking_id = ?)")) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            con.commit();

        } catch (SQLException e) {
            rollback(con);
            throw new DatabaseException("Cancellation failed and was undone: " + e.getMessage(), e);
        } catch (BookingCancellationException e) {
            rollback(con);
            throw e;
        } finally {
            close(con);
        }
    }

    // =====================================================================
    //  READ QUERIES
    // =====================================================================

    public List<Booking> getAllBookings() throws DatabaseException {
        return queryBookings(SELECT_BOOKINGS + GROUP_AND_ORDER, null);
    }

    public List<Booking> getBookingsByUser(int userId) throws DatabaseException {
        return queryBookings(SELECT_BOOKINGS + " WHERE b.user_id = ? " + GROUP_AND_ORDER, userId);
    }

    /** Returns the booking or null if the id does not exist. */
    public Booking getBookingById(int bookingId) throws DatabaseException {
        List<Booking> list = queryBookings(SELECT_BOOKINGS + " WHERE b.booking_id = ? " + GROUP_AND_ORDER, bookingId);
        return list.isEmpty() ? null : list.get(0);
    }

    // ---------- helper methods ----------

    private int findSeatId(Connection con, int showId, String seatNumber)
            throws SQLException, InvalidInputException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT seat_id FROM seats WHERE show_id = ? AND seat_number = ?")) {
            ps.setInt(1, showId);
            ps.setString(2, seatNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new InvalidInputException("Seat " + seatNumber + " does not exist for this show.");
                }
                return rs.getInt(1);
            }
        }
    }

    private List<Booking> queryBookings(String sql, Integer parameter) throws DatabaseException {
        List<Booking> bookings = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (parameter != null) {
                ps.setInt(1, parameter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
            }
            return bookings;
        } catch (SQLException e) {
            throw new DatabaseException("Could not load bookings: " + e.getMessage(), e);
        }
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setUserId(rs.getInt("user_id"));
        b.setCustomerName(rs.getString("full_name"));
        b.setShowId(rs.getInt("show_id"));
        b.setMovieTitle(rs.getString("title"));
        b.setShowDate(rs.getDate("show_date").toLocalDate());
        b.setShowTime(rs.getTime("show_time").toLocalTime());
        b.setScreenName(rs.getString("screen_name"));
        b.setBookingDate(rs.getTimestamp("booking_date").toLocalDateTime());
        b.setTotalAmount(rs.getDouble("total_amount"));
        b.setStatus(rs.getString("status"));
        b.setSeatCount(rs.getInt("seat_count"));
        b.setSeatNumbers(rs.getString("seat_numbers"));
        return b;
    }

    private void rollback(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException e) {
                System.err.println("Rollback failed: " + e.getMessage());
            }
        }
    }

    private void close(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                System.err.println("Could not close connection: " + e.getMessage());
            }
        }
    }
}
