package dao;

import database.DBConnection;
import exception.DatabaseException;
import model.Movie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** All SQL related to the 'movies' table. */
public class MovieDAO {

    private static final String SELECT_ALL =
            "SELECT movie_id, title, genre, language, duration_minutes, rating, description FROM movies";

    public List<Movie> getAllMovies() throws DatabaseException {
        String sql = SELECT_ALL + " ORDER BY movie_id";
        List<Movie> movies = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                movies.add(mapRow(rs));
            }
            return movies;
        } catch (SQLException e) {
            throw new DatabaseException("Could not load movies: " + e.getMessage(), e);
        }
    }

    /** Returns the movie or null if the id does not exist. */
    public Movie getMovieById(int movieId) throws DatabaseException {
        String sql = SELECT_ALL + " WHERE movie_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load movie: " + e.getMessage(), e);
        }
    }

    public int insert(Movie movie) throws DatabaseException {
        String sql = "INSERT INTO movies (title, genre, language, duration_minutes, rating, description) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, movie.getTitle());
            ps.setString(2, movie.getGenre());
            ps.setString(3, movie.getLanguage());
            ps.setInt(4, movie.getDurationMinutes());
            ps.setDouble(5, movie.getRating());
            ps.setString(6, movie.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new DatabaseException("Movie was saved but no ID was returned.");
        } catch (SQLException e) {
            throw new DatabaseException("Could not add movie: " + e.getMessage(), e);
        }
    }

    /** Returns true if a row was updated. */
    public boolean update(Movie movie) throws DatabaseException {
        String sql = "UPDATE movies SET title = ?, genre = ?, language = ?, duration_minutes = ?, "
                + "rating = ?, description = ? WHERE movie_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, movie.getTitle());
            ps.setString(2, movie.getGenre());
            ps.setString(3, movie.getLanguage());
            ps.setInt(4, movie.getDurationMinutes());
            ps.setDouble(5, movie.getRating());
            ps.setString(6, movie.getDescription());
            ps.setInt(7, movie.getMovieId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Could not update movie: " + e.getMessage(), e);
        }
    }

    /** Returns true if a row was deleted. Movies that already have bookings cannot be deleted. */
    public boolean delete(int movieId) throws DatabaseException {
        String sql = "DELETE FROM movies WHERE movie_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DatabaseException(
                    "This movie cannot be deleted because customers have already booked its shows.", e);
        } catch (SQLException e) {
            throw new DatabaseException("Could not delete movie: " + e.getMessage(), e);
        }
    }

    private Movie mapRow(ResultSet rs) throws SQLException {
        return new Movie(
                rs.getInt("movie_id"),
                rs.getString("title"),
                rs.getString("genre"),
                rs.getString("language"),
                rs.getInt("duration_minutes"),
                rs.getDouble("rating"),
                rs.getString("description"));
    }
}
