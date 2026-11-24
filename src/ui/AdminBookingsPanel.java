package ui;

import exception.DatabaseException;
import model.Booking;
import service.BookingService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Admin screen: every booking made by every customer. */
public class AdminBookingsPanel extends JPanel {

    private static final DateTimeFormatter BOOKED_ON = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final BookingService bookingService = new BookingService();
    private final DefaultTableModel model = Theme.readOnlyModel(
            "Booking ID", "Customer", "Movie", "Show Date", "Time", "Seats", "Amount", "Status", "Booked On");
    private final JTable table = new JTable(model);
    private final JLabel countLabel = Theme.label(" ", Theme.FONT_BOLD, Theme.TEXT_MUTED);

    public AdminBookingsPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(Theme.title("All Bookings"), BorderLayout.NORTH);
        add(Theme.scroll(table), BorderLayout.CENTER);

        JButton refresh = Theme.button("Refresh", Theme.ACCENT);
        refresh.addActionListener(e -> refreshData());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        bottom.setOpaque(false);
        bottom.add(refresh);
        bottom.add(countLabel);
        add(bottom, BorderLayout.SOUTH);
    }

    public void refreshData() {
        model.setRowCount(0);
        List<Booking> bookings;
        try {
            bookings = bookingService.getAllBookings();
        } catch (DatabaseException e) {
            Theme.showError(this, e.getMessage());
            bookings = new ArrayList<>();
        }
        for (Booking b : bookings) {
            model.addRow(new Object[]{b.getBookingId(), b.getCustomerName(), b.getMovieTitle(), b.getShowDate(),
                    b.getShowTime(), b.getSeatNumbers(), Theme.money(b.getTotalAmount()), b.getStatus(),
                    b.getBookingDate().format(BOOKED_ON)});
        }
        countLabel.setText("Total records: " + bookings.size());
    }
}
