package ui;

import exception.DatabaseException;
import model.Movie;
import model.Show;
import model.User;
import service.ShowService;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

/** Shows the details of a movie and its available showtimes. The customer picks a show here. */
public class MovieDetailsDialog extends JDialog {

    private final Movie movie;
    private final User user;
    private final Runnable onBooked;
    private final ShowService showService = new ShowService();

    private final DefaultTableModel model =
            Theme.readOnlyModel("Show ID", "Date", "Time", "Screen", "Price", "Seats Left");
    private final JTable table = new JTable(model);
    private List<Show> shows = new ArrayList<>();

    public MovieDetailsDialog(Window owner, Movie movie, User user, Runnable onBooked) {
        super(owner, "Movie Details", Dialog.ModalityType.APPLICATION_MODAL);
        this.movie = movie;
        this.user = user;
        this.onBooked = onBooked;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(Theme.BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        content.add(buildDetails(), BorderLayout.NORTH);

        JPanel showPanel = new JPanel(new BorderLayout(0, 6));
        showPanel.setOpaque(false);
        showPanel.add(Theme.label("Available showtimes", Theme.FONT_HEADING, Theme.TEXT_DARK), BorderLayout.NORTH);
        JScrollPane scroll = Theme.scroll(table);
        scroll.setPreferredSize(new Dimension(560, 170));
        showPanel.add(scroll, BorderLayout.CENTER);
        content.add(showPanel, BorderLayout.CENTER);

        JButton select = Theme.button("Select Seats", Theme.SUCCESS);
        JButton close = Theme.button("Close", Theme.SIDEBAR);
        select.addActionListener(e -> openSeatSelection());
        close.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(close);
        buttons.add(select);
        content.add(buttons, BorderLayout.SOUTH);

        setContentPane(content);
        loadShows();
        pack();
        setLocationRelativeTo(owner);
    }

    private JPanel buildDetails() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        panel.add(Theme.label(movie.getTitle(), Theme.FONT_TITLE, Theme.TEXT_DARK));
        panel.add(Theme.label(movie.getGenre() + "  |  " + movie.getLanguage() + "  |  "
                + movie.getDurationMinutes() + " min  |  Rating " + movie.getRating() + "/10",
                Theme.FONT_BOLD, Theme.ACCENT));
        panel.add(javax.swing.Box.createRigidArea(new Dimension(0, 8)));

        JTextArea description = new JTextArea(movie.getDescription() == null || movie.getDescription().isEmpty()
                ? "No description available." : movie.getDescription());
        description.setFont(Theme.FONT_NORMAL);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setEditable(false);
        description.setOpaque(false);
        description.setRows(3);
        description.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(description);
        return panel;
    }

    private void loadShows() {
        model.setRowCount(0);
        try {
            shows = showService.getUpcomingShowsForMovie(movie.getMovieId());
        } catch (DatabaseException e) {
            Theme.showError(this, e.getMessage());
            shows = new ArrayList<>();
        }
        for (Show show : shows) {
            model.addRow(new Object[]{show.getShowId(), show.getShowDate(), show.getShowTime(),
                    show.getScreenName(), Theme.money(show.getTicketPrice()),
                    show.getAvailableSeats() + " / " + show.getTotalSeats()});
        }
    }

    private void openSeatSelection() {
        if (shows.isEmpty()) {
            Theme.showInfo(this, "There are no upcoming showtimes for this movie.");
            return;
        }
        int row = table.getSelectedRow();
        if (row < 0) {
            Theme.showInfo(this, "Please select a showtime from the table first.");
            return;
        }
        Show show = shows.get(row);
        new SeatSelectionDialog(this, show, user, false, () -> {
            loadShows();
            if (onBooked != null) {
                onBooked.run();
            }
        }).setVisible(true);
    }
}
