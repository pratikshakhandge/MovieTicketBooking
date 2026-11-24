package ui;

import exception.DatabaseException;
import exception.InvalidInputException;
import model.Movie;
import model.Seat;
import model.Show;
import service.MovieService;
import service.ShowService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Admin screen: add showtimes and see available / booked seats of every show. */
public class AdminShowsPanel extends JPanel {

    private final ShowService showService = new ShowService();
    private final MovieService movieService = new MovieService();

    private final DefaultTableModel model = Theme.readOnlyModel(
            "Show ID", "Movie", "Date", "Time", "Screen", "Price", "Total Seats", "Available", "Booked");
    private final JTable table = new JTable(model);
    private List<Show> shows = new ArrayList<>();

    private final JComboBox<Movie> movieCombo = new JComboBox<>();
    private final JTextField dateField = Theme.inputField(9);
    private final JTextField timeField = Theme.inputField(5);
    private final JComboBox<String> screenCombo = new JComboBox<>(new String[]{"Screen 1", "Screen 2", "Screen 3"});
    private final JTextField priceField = Theme.inputField(6);

    public AdminShowsPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(Theme.title("Showtimes and Seats"), BorderLayout.NORTH);
        add(Theme.scroll(table), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.SOUTH);

        dateField.setText(LocalDate.now().plusDays(1).toString());
        timeField.setText("18:00");
        priceField.setText("250");
    }

    private JPanel buildForm() {
        JPanel form = Theme.card();
        form.setLayout(new BorderLayout(0, 8));

        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fields.setOpaque(false);
        fields.add(Theme.formLabel("Movie"));
        fields.add(movieCombo);
        fields.add(Theme.formLabel("Date (yyyy-MM-dd)"));
        fields.add(dateField);
        fields.add(Theme.formLabel("Time (HH:mm)"));
        fields.add(timeField);
        fields.add(Theme.formLabel("Screen"));
        fields.add(screenCombo);
        fields.add(Theme.formLabel("Price (Rs.)"));
        fields.add(priceField);

        JButton add = Theme.button("Add Showtime", Theme.SUCCESS);
        JButton viewSeats = Theme.button("View Seats of Selected Show", Theme.ACCENT);
        JButton refresh = Theme.button("Refresh", Theme.SIDEBAR);
        add.addActionListener(e -> addShow());
        viewSeats.addActionListener(e -> viewSeats());
        refresh.addActionListener(e -> refreshData());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(add);
        buttons.add(viewSeats);
        buttons.add(refresh);

        form.add(fields, BorderLayout.CENTER);
        form.add(buttons, BorderLayout.SOUTH);
        return form;
    }

    public void refreshData() {
        // reload movies (keep current selection if possible)
        Object previous = movieCombo.getSelectedItem();
        movieCombo.removeAllItems();
        try {
            for (Movie movie : movieService.getAllMovies()) {
                movieCombo.addItem(movie);
                if (previous instanceof Movie && ((Movie) previous).getMovieId() == movie.getMovieId()) {
                    movieCombo.setSelectedItem(movie);
                }
            }
            shows = showService.getAllShows();
        } catch (DatabaseException e) {
            Theme.showError(this, e.getMessage());
            shows = new ArrayList<>();
        }

        model.setRowCount(0);
        for (Show s : shows) {
            model.addRow(new Object[]{s.getShowId(), s.getMovieTitle(), s.getShowDate(), s.getShowTime(),
                    s.getScreenName(), Theme.money(s.getTicketPrice()), s.getTotalSeats(),
                    s.getAvailableSeats(), s.getBookedSeats()});
        }
    }

    private void addShow() {
        Movie movie = (Movie) movieCombo.getSelectedItem();
        if (movie == null) {
            Theme.showError(this, "Please add a movie first.");
            return;
        }
        try {
            int showId = showService.addShow(movie.getMovieId(), dateField.getText(), timeField.getText(),
                    (String) screenCombo.getSelectedItem(), priceField.getText());
            Theme.showInfo(this, "Showtime added (Show ID " + showId + ") with "
                    + Seat.TOTAL_SEATS + " seats.");
            refreshData();
        } catch (InvalidInputException | DatabaseException e) {
            Theme.showError(this, e.getMessage());
        }
    }

    private void viewSeats() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Theme.showInfo(this, "Please select a show from the table first.");
            return;
        }
        new SeatSelectionDialog(SwingUtilities.getWindowAncestor(this), shows.get(row), null, true, null)
                .setVisible(true);
    }
}
