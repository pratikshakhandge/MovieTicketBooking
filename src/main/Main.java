package main;

import database.DBConnection;
import exception.DatabaseException;
import ui.LoginFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.Connection;
import java.sql.SQLException;

/** Entry point of the Movie Ticket Booking Management System. */
public class Main {

    public static void main(String[] args) {
        // Cross-platform look and feel: our button colours look the same on every computer
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }

        // Check the database BEFORE opening any window
        try (Connection con = DBConnection.getConnection()) {
            if (!con.isValid(3)) {
                throw new DatabaseException("The database connection is not valid.");
            }
        } catch (DatabaseException | SQLException e) {
            JOptionPane.showMessageDialog(null,
                    e.getMessage() + "\n\nCheck MySQL (port 3307) and the password in DBConnection.java.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // All Swing windows must be created on the Event Dispatch Thread (the GUI thread)
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
