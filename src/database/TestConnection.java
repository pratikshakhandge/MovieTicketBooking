package database;

import exception.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A small program to test the database setup.
 * We will not use it in the final app, it is only for checking.
 */
public class TestConnection {

    public static void main(String[] args) {
        System.out.println("Testing database connection...");

        // try-with-resources: the connection is closed automatically
        try (Connection con = DBConnection.getConnection()) {

            System.out.println("SUCCESS: Connected to " + con.getCatalog());

            String[] tables = {"users", "movies", "shows", "seats", "bookings", "booking_seats"};
            for (String table : tables) {
                String sql = "SELECT COUNT(*) FROM " + table;   // table names are fixed here, not user input
                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println(table + " -> " + rs.getInt(1) + " rows");
                    }
                }
            }

            // Test a query with a parameter (this is how all our DAOs will work)
            String sql = "SELECT full_name, role FROM users WHERE email = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, "admin@movie.com");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Admin account found: " + rs.getString("full_name")
                                + " (" + rs.getString("role") + ")");
                    }
                }
            }

        } catch (DatabaseException e) {
            System.out.println("CONNECTION FAILED: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("Technical reason: " + e.getCause().getMessage());
            }
        } catch (SQLException e) {
            System.out.println("QUERY FAILED: " + e.getMessage());
        }
    }
}
