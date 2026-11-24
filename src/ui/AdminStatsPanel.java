package ui;

import analytics.AnalyticsService;
import analytics.DashboardStats;
import analytics.MovieStat;
import exception.DatabaseException;
import exception.InvalidInputException;
import model.Booking;
import service.Validator;
import threads.DashboardRefresher;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Admin screen: statistics calculated with Java Streams.
 * The numbers refresh automatically every 10 seconds using a background thread
 * (DashboardRefresher). The "Refresh Now" button uses a SwingWorker.
 */
public class AdminStatsPanel extends JPanel {

    private static final int REFRESH_SECONDS = 10;

    private final AnalyticsService analyticsService = new AnalyticsService();
    private final DashboardRefresher refresher;

    private final JLabel totalBookingsLabel = valueLabel();
    private final JLabel revenueLabel = valueLabel();
    private final JLabel ticketsLabel = valueLabel();
    private final JLabel averageLabel = valueLabel();
    private final JLabel popularLabel = valueLabel();
    private final JLabel seatsLabel = Theme.label(" ", Theme.FONT_BOLD, Theme.TEXT_DARK);
    private final JLabel updatedLabel = Theme.label("Not loaded yet", Theme.FONT_NORMAL, Theme.TEXT_MUTED);

    private final DefaultTableModel model = Theme.readOnlyModel("Rank", "Movie", "Bookings", "Revenue");
    private final JTable table = new JTable(model);

    private final JTextField fromField = Theme.inputField(9);
    private final JTextField toField = Theme.inputField(9);
    private final JLabel filterResult = Theme.label(" ", Theme.FONT_BOLD, Theme.TEXT_DARK);

    public AdminStatsPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(Theme.title("Booking Statistics"), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(buildCards(), BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 6));
        tablePanel.setOpaque(false);
        tablePanel.add(Theme.label("Popular movies (sorted by bookings)", Theme.FONT_HEADING, Theme.TEXT_DARK),
                BorderLayout.NORTH);
        tablePanel.add(Theme.scroll(table), BorderLayout.CENTER);
        center.add(tablePanel, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        add(buildBottom(), BorderLayout.SOUTH);

        fromField.setText(LocalDate.now().minusDays(30).toString());
        toField.setText(LocalDate.now().toString());

        // start the automatic refresh thread
        refresher = new DashboardRefresher(analyticsService, REFRESH_SECONDS, this::showStats, this::showRefreshError);
        refresher.start();
    }

    private static JLabel valueLabel() {
        return Theme.label("-", new java.awt.Font("SansSerif", java.awt.Font.BOLD, 22), Theme.TEXT_DARK);
    }

    private JPanel buildCards() {
        JPanel cards = new JPanel(new GridLayout(1, 5, 12, 0));
        cards.setOpaque(false);
        cards.add(statCard("Total Bookings", totalBookingsLabel, Theme.ACCENT));
        cards.add(statCard("Total Revenue", revenueLabel, Theme.SUCCESS));
        cards.add(statCard("Tickets Sold", ticketsLabel, Theme.WARNING));
        cards.add(statCard("Avg Ticket Price", averageLabel, Theme.SIDEBAR));
        cards.add(statCard("Most Popular Movie", popularLabel, Theme.DANGER));
        return cards;
    }

    private JPanel statCard(String title, JLabel value, Color accent) {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 0, 0, 0, accent),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        card.add(Theme.label(title, Theme.FONT_BOLD, Theme.TEXT_MUTED), BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildBottom() {
        JPanel bottom = new JPanel(new GridLayout(3, 1, 0, 6));
        bottom.setOpaque(false);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row1.setOpaque(false);
        JButton refreshNow = Theme.button("Refresh Now", Theme.ACCENT);
        refreshNow.addActionListener(e -> refreshWithWorker(refreshNow));
        row1.add(refreshNow);
        row1.add(seatsLabel);
        row1.add(updatedLabel);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row2.setOpaque(false);
        JButton filter = Theme.button("Filter by Booking Date", Theme.SIDEBAR);
        filter.addActionListener(e -> filterByDate());
        row2.add(Theme.formLabel("From (yyyy-MM-dd)"));
        row2.add(fromField);
        row2.add(Theme.formLabel("To"));
        row2.add(toField);
        row2.add(filter);

        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row3.setOpaque(false);
        row3.add(filterResult);

        bottom.add(row1);
        bottom.add(row2);
        bottom.add(row3);
        return bottom;
    }

    /** Called on the GUI thread (by the background thread through invokeLater, or by the SwingWorker). */
    private void showStats(DashboardStats stats) {
        totalBookingsLabel.setText(String.valueOf(stats.totalBookings()));
        revenueLabel.setText(Theme.money(stats.totalRevenue()));
        ticketsLabel.setText(String.valueOf(stats.ticketsSold()));
        averageLabel.setText(Theme.money(stats.averageTicketPrice()));
        popularLabel.setText(stats.mostPopularMovie());

        int percent = stats.seatsTotal() == 0 ? 0 : (int) Math.round(stats.seatsBooked() * 100.0 / stats.seatsTotal());
        seatsLabel.setText("Seats booked: " + stats.seatsBooked() + " / " + stats.seatsTotal() + " (" + percent + "%)");
        updatedLabel.setText("Last updated: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                + "  (auto-refresh every " + REFRESH_SECONDS + " s)");

        model.setRowCount(0);
        int rank = 1;
        for (MovieStat stat : stats.movieStats()) {
            model.addRow(new Object[]{rank++, stat.movieTitle(), stat.bookings(), Theme.money(stat.revenue())});
        }
    }

    private void showRefreshError(String message) {
        updatedLabel.setText("Refresh failed: " + message);
    }

    /** Loads the first values when the admin opens this screen. */
    public void refreshData() {
        refreshWithWorker(null);
    }

    /**
     * SwingWorker: doInBackground() runs on a worker thread (database work),
     * done() runs on the GUI thread (updates the labels). The window never freezes.
     */
    private void refreshWithWorker(JButton button) {
        if (button != null) {
            button.setEnabled(false);
        }
        new SwingWorker<DashboardStats, Void>() {
            @Override
            protected DashboardStats doInBackground() throws DatabaseException {
                return analyticsService.loadDashboardStats();
            }

            @Override
            protected void done() {
                try {
                    showStats(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Theme.showError(AdminStatsPanel.this, e.getCause().getMessage());
                } finally {
                    if (button != null) {
                        button.setEnabled(true);
                    }
                }
            }
        }.execute();
    }

    private void filterByDate() {
        try {
            LocalDate from = Validator.parseDate(fromField.getText(), "From date");
            LocalDate to = Validator.parseDate(toField.getText(), "To date");
            List<Booking> found = analyticsService.getBookingsBetween(from, to);
            filterResult.setText("Confirmed bookings from " + from + " to " + to + ": " + found.size()
                    + "   |   Revenue: " + Theme.money(analyticsService.revenueOf(found)));
        } catch (InvalidInputException | DatabaseException e) {
            Theme.showError(this, e.getMessage());
        }
    }

    /** Called when the admin window closes: stops the background thread. */
    public void stop() {
        refresher.stop();
    }
}
