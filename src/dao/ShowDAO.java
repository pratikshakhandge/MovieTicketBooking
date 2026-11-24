package dao;

import database.DBConnection;
import exception.DatabaseException;
import model.Seat;
import model.Show;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/** All SQL related to the 'shows' table. */
public class ShowDAO {

    /** Shows joined with the movie title and the number of free seats. */
    private static final String SELECT_SHOWS =
            "SELECT s.show_id, s.movie_id, m.title, s.show_date, s.show_time, s.screen_name, "
                    + "s.ticket_price, s.total_seats, "
                    + "(SELECT COUNT(*) FROM seats st WHERE st.show_id = s.show_id AND st.is_booked = FALSE) "
                    + "AS available_seats "
                    + "FROM shows s JOIN movies m ON s.movie_id = m.movie_id";

    private static final String ORDER_BY = " ORDER BY s.show_date, s.show_time";

    public List<Show> getAllShows() throws DatabaseException {
        return queryShows(SELECT_SHOWS + ORDER_BY, null);
    }

    public List<Show> getShowsByMovie(int movieId) throws DatabaseException {
        return queryShows(SELECT_SHOWS + " WHERE s.movie_id = ?" + ORDER_BY, movieId);
    }

    /** Returns the show or null if the id does not exist. */
    public Show getShowById(int showId) throws DatabaseException {
        List<Show> list = queryShows(SELECT_SHOWS + " WHERE s.show_id = ?", showId);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Adds a new show AND creates all its seats.
     * Both steps are done in one transaction: either everything is saved or nothing.
     */
    public int insertShow(Show show) throws DatabaseException {
        String showSql = "INSERT INTO shows (movie_id, show_date, show_time, screen_name, ticket_price, total_seats) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String seatSql = "INSERT INTO seats (show_id, seat_number) VALUES (?, ?)";

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);              // start transaction

            int showId;
            try (PreparedStatement ps = con.prepareStatement(showSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, show.getMovieId());
                ps.setDate(2, Date.valueOf(show.getShowDate()));
                ps.setTime(3, Time.valueOf(show.getShowTime()));
                ps.setString(4, show.getScreenName());
                ps.setDouble(5, show.getTicketPrice());
                ps.setInt(6, Seat.TOTAL_SEATS);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No show ID was returned.");
                    }
                    showId = keys.getInt(1);
                }
            }

            try (PreparedStatement ps = con.prepareStatement(seatSql)) {
                for (String row : Seat.ROWS) {
                    for (int col = 1; col <= Seat.COLUMNS; col++) {
                        ps.setInt(1, showId);
                        ps.setString(2, row + col);
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }

            con.commit();                          // save everything
            return showId;

        } catch (SQLException e) {
            rollback(con);
            throw new DatabaseException("Could not add show (nothing was saved): " + e.getMessage(), e);
        } finally {
            close(con);
        }
    }

    // ---------- helper methods ----------

    private List<Show> queryShows(String sql, Integer parameter) throws DatabaseException {
        List<Show> shows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (parameter != null) {
                ps.setInt(1, parameter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    shows.add(mapRow(rs));
                }
            }
            return shows;
        } catch (SQLException e) {
            throw new DatabaseException("Could not load shows: " + e.getMessage(), e);
        }
    }

    private Show mapRow(ResultSet rs) throws SQLException {
        Show show = new Show();
        show.setShowId(rs.getInt("show_id"));
        show.setMovieId(rs.getInt("movie_id"));
        show.setMovieTitle(rs.getString("title"));
        show.setShowDate(rs.getDate("show_date").toLocalDate());
        show.setShowTime(rs.getTime("show_time").toLocalTime());
        show.setScreenName(rs.getString("screen_name"));
        show.setTicketPrice(rs.getDouble("ticket_price"));
        show.setTotalSeats(rs.getInt("total_seats"));
        show.setAvailableSeats(rs.getInt("available_seats"));
        return show;
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
