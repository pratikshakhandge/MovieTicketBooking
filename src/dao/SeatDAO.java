package dao;

import database.DBConnection;
import exception.DatabaseException;
import model.Seat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** All SQL related to the 'seats' table. */
public class SeatDAO {

    public List<Seat> getSeatsByShow(int showId) throws DatabaseException {
        String sql = "SELECT seat_id, show_id, seat_number, is_booked FROM seats "
                + "WHERE show_id = ? ORDER BY seat_number";
        List<Seat> seats = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seats.add(new Seat(rs.getInt("seat_id"), rs.getInt("show_id"),
                            rs.getString("seat_number"), rs.getBoolean("is_booked")));
                }
            }
            return seats;
        } catch (SQLException e) {
            throw new DatabaseException("Could not load seats: " + e.getMessage(), e);
        }
    }

    public int countAvailableSeats(int showId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM seats WHERE show_id = ? AND is_booked = FALSE";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not count seats: " + e.getMessage(), e);
        }
    }
}
