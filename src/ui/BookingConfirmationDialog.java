package ui;

import model.Booking;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.format.DateTimeFormatter;

/** Shows the ticket: used for the booking confirmation and for "View Ticket" in booking history. */
public class BookingConfirmationDialog extends JDialog {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    public BookingConfirmationDialog(Window owner, Booking booking, boolean justBooked) {
        super(owner, justBooked ? "Booking Confirmed" : "Ticket Details", Dialog.ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Theme.BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // header
        boolean confirmed = booking.isConfirmed();
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(confirmed ? Theme.SUCCESS : Theme.DANGER);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        String headline = justBooked ? "Booking Confirmed!" : (confirmed ? "Your Ticket" : "Booking Cancelled");
        header.add(Theme.label(headline, Theme.FONT_TITLE, Color.WHITE));
        header.add(Theme.label("Booking ID: #" + booking.getBookingId(), Theme.FONT_HEADING, Color.WHITE));

        // ticket details
        JPanel details = Theme.card();
        details.setLayout(new GridBagLayout());
        int row = 0;
        addLine(details, "Movie", booking.getMovieTitle(), row++);
        addLine(details, "Customer", booking.getCustomerName(), row++);
        addLine(details, "Date", booking.getShowDate().format(DATE_FORMAT), row++);
        addLine(details, "Time", booking.getShowTime().toString(), row++);
        addLine(details, "Screen", booking.getScreenName(), row++);
        addLine(details, "Seats", booking.getSeatNumbers() + "  (" + booking.getSeatCount() + " tickets)", row++);
        addLine(details, "Total paid", Theme.money(booking.getTotalAmount()), row++);
        addLine(details, "Status", booking.getStatus(), row++);
        addLine(details, "Booked on", booking.getBookingDate().format(DATE_TIME_FORMAT), row);

        JButton close = Theme.button("Close", Theme.ACCENT);
        close.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setBackground(Theme.BACKGROUND);
        buttons.add(close);

        content.add(header, BorderLayout.NORTH);
        content.add(details, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        setContentPane(content);

        pack();
        setMinimumSize(new java.awt.Dimension(420, 0));
        setLocationRelativeTo(owner);
    }

    private void addLine(JPanel panel, String label, String value, int row) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 0, 5, 24);
        panel.add(Theme.label(label, Theme.FONT_BOLD, Theme.TEXT_MUTED), c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = row;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        c.insets = new Insets(5, 0, 5, 0);
        panel.add(Theme.label(value, Theme.FONT_HEADING, Theme.TEXT_DARK), c);
    }
}
