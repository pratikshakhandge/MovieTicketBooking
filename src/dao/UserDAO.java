package dao;

import database.DBConnection;
import exception.DatabaseException;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** All SQL related to the 'users' table. */
public class UserDAO {

    /** Returns the user with this email, or null if there is none. */
    public User findByEmail(String email) throws DatabaseException {
        String sql = "SELECT user_id, full_name, email, password, role FROM users WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("user_id"), rs.getString("full_name"),
                            rs.getString("email"), rs.getString("password"), rs.getString("role"));
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read user data: " + e.getMessage(), e);
        }
    }

    /** Saves a new user and returns the generated user_id. */
    public int insert(User user) throws DatabaseException {
        String sql = "INSERT INTO users (full_name, email, password, role) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new DatabaseException("User was saved but no ID was returned.");
        } catch (SQLException e) {
            throw new DatabaseException("Could not save user: " + e.getMessage(), e);
        }
    }
}
