package ui;

import exception.DatabaseException;
import exception.InvalidInputException;
import exception.InvalidShowException;
import exception.NoSeatsAvailableException;
import exception.SeatAlreadyBookedException;
import model.Booking;
import model.Seat;
import model.Show;
import model.User;
import service.BookingService;
import service.ShowService;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Seat map made of Swing buttons.
 *  green = available, blue = selected by you, red = already booked.
 * Customers use it to book seats; the admin opens it in view-only mode.
 */
public class SeatSelectionDialog extends JDialog {

    private static final Color AVAILABLE = new Color(22, 163, 74);
    private static final Color SELECTED = new Color(37, 99, 235);
    private static final Color BOOKED = new Color(220, 38, 38);

    private final Show show;
    private final User user;
    private final boolean viewOnly;
    private final Runnable onBooked;

    private final ShowService showService = new ShowService();
    private final BookingService bookingService = new BookingService();

    private final JPanel seatGrid = new JPanel(new GridLayout(Seat.ROWS.length, Seat.COLUMNS, 8, 8));
    private final JLabel infoLabel = Theme.label(" ", Theme.FONT_HEADING, Theme.TEXT_DARK);
    private final JLabel summaryLabel = Theme.label(" ", Theme.FONT_HEADING, Theme.TEXT_DARK);
    private final JButton bookButton = Theme.button("Book Selected Seats", Theme.SUCCESS);

    private final TreeSet<String> selectedSeats = new TreeSet<>();
    private final Map<String, JButton> buttons = new HashMap<>();

    public SeatSelectionDialog(Window owner, Show show, User user, boolean viewOnly, Runnable onBooked) {
        super(owner, viewOnly ? "Seat Status" : "Select Your Seats", Dialog.ModalityType.APPLICATION_MODAL);
        this.show = show;
        this.user = user;
        this.viewOnly = viewOnly;
        this.onBooked = onBooked;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(Theme.BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        content.add(buildTop(), BorderLayout.NORTH);

        seatGrid.setOpaque(false);
        JPanel gridWrapper = Theme.card();
        gridWrapper.setLayout(new BorderLayout());
        gridWrapper.add(seatGrid, BorderLayout.CENTER);
        content.add(gridWrapper, BorderLayout.CENTER);
        content.add(buildBottom(), BorderLayout.SOUTH);

        setContentPane(content);
        loadSeats();
        pack();
        setLocationRelativeTo(owner);
    }

    private JPanel buildTop() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        top.add(Theme.label(show.getMovieTitle(), Theme.FONT_TITLE, Theme.TEXT_DARK));
        top.add(Theme.label(show.getShowDate() + "  " + show.getShowTime() + "  |  " + show.getScreenName()
                + "  |  " + Theme.money(show.getTicketPrice()) + " per seat", Theme.FONT_NORMAL, Theme.TEXT_MUTED));
        top.add(javax.swing.Box.createRigidArea(new Dimension(0, 10)));

        JLabel screen = new JLabel("SCREEN THIS WAY", SwingConstants.CENTER);
        screen.setFont(Theme.FONT_BOLD);
        screen.setForeground(Color.WHITE);
        screen.setOpaque(true);
        screen.setBackground(Theme.SIDEBAR);
        screen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        screen.setAlignmentX(LEFT_ALIGNMENT);
        top.add(screen);
        return top;
    }

    private JPanel buildBottom() {
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        legend.setOpaque(false);
        legend.add(legendItem("Available", AVAILABLE));
        legend.add(legendItem("Selected", SELECTED));
        legend.add(legendItem("Booked", BOOKED));
        legend.setAlignmentX(LEFT_ALIGNMENT);

        infoLabel.setAlignmentX(LEFT_ALIGNMENT);
        summaryLabel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(LEFT_ALIGNMENT);
        JButton close = Theme.button(viewOnly ? "Close" : "Cancel", Theme.SIDEBAR);
        close.addActionListener(e -> dispose());
        buttonRow.add(close);
        if (!viewOnly) {
            bookButton.addActionListener(e -> bookSelectedSeats());
            buttonRow.add(bookButton);
        }

        bottom.add(legend);
        bottom.add(javax.swing.Box.createRigidArea(new Dimension(0, 8)));
        bottom.add(infoLabel);
        bottom.add(javax.swing.Box.createRigidArea(new Dimension(0, 4)));
        if (!viewOnly) {
            bottom.add(summaryLabel);
        }
        bottom.add(javax.swing.Box.createRigidArea(new Dimension(0, 8)));
        bottom.add(buttonRow);
        return bottom;
    }

    private JPanel legendItem(String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        item.setOpaque(false);
        JLabel box = new JLabel("    ");
        box.setOpaque(true);
        box.setBackground(color);
        item.add(box);
        item.add(Theme.label(text, Theme.FONT_NORMAL, Theme.TEXT_DARK));
        return item;
    }

    /** Reads the seats from the database and (re)builds the buttons. */
    private void loadSeats() {
        seatGrid.removeAll();
        buttons.clear();

        List<Seat> seats;
        try {
            seats = showService.getSeats(show.getShowId());
        } catch (DatabaseException e) {
            Theme.showError(this, e.getMessage());
            seats = new ArrayList<>();
        }
        Map<String, Seat> seatByNumber = new HashMap<>();
        for (Seat seat : seats) {
            seatByNumber.put(seat.getSeatNumber(), seat);
        }

        int available = 0;
        int booked = 0;
        for (String row : Seat.ROWS) {
            for (int col = 1; col <= Seat.COLUMNS; col++) {
                String number = row + col;
                Seat seat = seatByNumber.get(number);
                JButton button = new JButton(number);
                button.setFont(Theme.FONT_BOLD);
                button.setForeground(Color.WHITE);
                button.setOpaque(true);
                button.setContentAreaFilled(true);
                button.setBorderPainted(false);
                button.setFocusPainted(false);
                button.setPreferredSize(new Dimension(62, 40));

                if (seat == null || seat.isBooked()) {
                    button.setBackground(BOOKED);
                    booked++;
                } else {
                    button.setBackground(selectedSeats.contains(number) ? SELECTED : AVAILABLE);
                    available++;
                    if (!viewOnly) {
                        button.addActionListener(e -> toggleSeat(number));
                    }
                }
                buttons.put(number, button);
                seatGrid.add(button);
            }
        }
        selectedSeats.removeIf(number -> seatByNumber.get(number) == null || seatByNumber.get(number).isBooked());

        infoLabel.setText("Available seats: " + available + "    Booked seats: " + booked);
        updateSummary();
        seatGrid.revalidate();
        seatGrid.repaint();
    }

    private void toggleSeat(String number) {
        JButton button = buttons.get(number);
        if (selectedSeats.contains(number)) {
            selectedSeats.remove(number);
            button.setBackground(AVAILABLE);
        } else {
            if (selectedSeats.size() >= BookingService.MAX_SEATS_PER_BOOKING) {
                Theme.showInfo(this, "You can select at most " + BookingService.MAX_SEATS_PER_BOOKING
                        + " seats in one booking.");
                return;
            }
            selectedSeats.add(number);
            button.setBackground(SELECTED);
        }
        updateSummary();
    }

    private void updateSummary() {
        if (selectedSeats.isEmpty()) {
            summaryLabel.setText("No seats selected.");
            bookButton.setEnabled(false);
        } else {
            double total = BookingService.calculateTotal(show.getTicketPrice(), selectedSeats.size());
            summaryLabel.setText("Selected: " + String.join(", ", selectedSeats)
                    + "   |   Total: " + Theme.money(total));
            bookButton.setEnabled(true);
        }
    }

    private void bookSelectedSeats() {
        List<String> seats = new ArrayList<>(selectedSeats);
        double total = BookingService.calculateTotal(show.getTicketPrice(), seats.size());
        if (!Theme.confirm(this, "Book seats " + String.join(", ", seats) + " for "
                + Theme.money(total) + "?")) {
            return;
        }
        try {
            Booking booking = bookingService.bookSeats(user, show.getShowId(), seats);
            dispose();
            if (onBooked != null) {
                onBooked.run();
            }
            new BookingConfirmationDialog(getOwner(), booking, true).setVisible(true);
        } catch (SeatAlreadyBookedException e) {
            Theme.showError(this, e.getMessage());
            loadSeats();                                   // show the latest seat status
        } catch (NoSeatsAvailableException | InvalidShowException
                 | InvalidInputException | DatabaseException e) {
            Theme.showError(this, e.getMessage());
            loadSeats();
        }
    }
}
