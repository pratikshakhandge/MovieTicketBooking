package database;

import exception.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Creates connections to the MySQL database.
 * Every DAO class will call DBConnection.getConnection().
 */
public class DBConnection {

    // ====== CHANGE ONLY THE PASSWORD BELOW ======
    private static final String HOST     = "localhost";
    private static final int    PORT     = 3307;
    private static final String DB_NAME  = "movie_booking_system";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";   // <-- put your password here
    // =============================================

    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    // Private constructor: this class only has static methods, nobody should create objects of it
    private DBConnection() {
    }

    /**
     * Opens a NEW connection every time it is called.
     * The caller must close it (we use try-with-resources for this).
     */
    public static Connection getConnection() throws DatabaseException {
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Cannot connect to the database. Check that MySQL is running on port "
                            + PORT + " and the password in DBConnection.java is correct.", e);
        }
    }
}
