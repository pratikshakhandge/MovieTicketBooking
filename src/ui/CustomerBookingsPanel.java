package ui;

import exception.BookingCancellationException;
import exception.DatabaseException;
import model.Booking;
import model.User;
import service.BookingService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Customer screen: booking history, view ticket and cancel booking. */
public class CustomerBookingsPanel extends JPanel {

    private static final DateTimeFormatter BOOKED_ON = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final User user;
    private final BookingService bookingService = new BookingService();
    private final DefaultTableModel model = Theme.readOnlyModel(
            "Booking ID", "Movie", "Show Date", "Time", "Screen", "Seats", "Amount", "Status", "Booked On");
    private final JTable table = new JTable(model);
    private List<Booking> bookings = new ArrayList<>();

    public CustomerBookingsPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(Theme.title("My Bookings"), BorderLayout.NORTH);
        add(Theme.scroll(table), BorderLayout.CENTER);

        JButton view = Theme.button("View Ticket", Theme.ACCENT);
        JButton cancel = Theme.button("Cancel Booking", Theme.DANGER);
        JButton refresh = Theme.button("Refresh", Theme.SIDEBAR);
        view.addActionListener(e -> viewTicket());
        cancel.addActionListener(e -> cancelBooking());
        refresh.addActionListener(e -> refreshData());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(view);
        buttons.add(cancel);
        buttons.add(refresh);
        buttons.add(Theme.label("Rule: you can cancel until " + BookingService.CANCEL_BEFORE_HOURS
                + " hours before the show.", Theme.FONT_NORMAL, Theme.TEXT_MUTED));
        add(buttons, BorderLayout.SOUTH);
    }

    public void refreshData() {
        model.setRowCount(0);
        try {
            bookings = bookingService.getBookingsForUser(user.getUserId());
        } catch (DatabaseException e) {
            Theme.showError(this, e.getMessage());
            bookings = new ArrayList<>();
        }
        for (Booking b : bookings) {
            model.addRow(new Object[]{b.getBookingId(), b.getMovieTitle(), b.getShowDate(), b.getShowTime(),
                    b.getScreenName(), b.getSeatNumbers(), Theme.money(b.getTotalAmount()), b.getStatus(),
                    b.getBookingDate().format(BOOKED_ON)});
        }
    }

    private Booking selectedBooking() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Theme.showInfo(this, "Please select a booking from the table first.");
            return null;
        }
        return bookings.get(row);
    }

    private void viewTicket() {
        Booking booking = selectedBooking();
        if (booking != null) {
            new BookingConfirmationDialog(SwingUtilities.getWindowAncestor(this), booking, false).setVisible(true);
        }
    }

    private void cancelBooking() {
        Booking booking = selectedBooking();
        if (booking == null) {
            return;
        }
        if (!Theme.confirm(this, "Cancel booking #" + booking.getBookingId() + " for \""
                + booking.getMovieTitle() + "\"?")) {
            return;
        }
        try {
            bookingService.cancelBooking(user, booking.getBookingId());
            Theme.showInfo(this, "Booking #" + booking.getBookingId() + " was cancelled. Your seats are free again.");
        } catch (BookingCancellationException | DatabaseException e) {
            Theme.showError(this, e.getMessage());
        }
        refreshData();
    }
}
