package ui;

import exception.DatabaseException;
import model.Movie;
import model.User;
import service.MovieService;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.List;

/** Customer screen: movies shown as cards. */
public class CustomerMoviesPanel extends JPanel {

    private final User user;
    private final Runnable onBooked;
    private final MovieService movieService = new MovieService();
    private final JPanel cardGrid = new JPanel(new GridLayout(0, 3, 16, 16));

    public CustomerMoviesPanel(User user, Runnable onBooked) {
        this.user = user;
        this.onBooked = onBooked;
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(Theme.title("Now Showing"), BorderLayout.NORTH);

        cardGrid.setOpaque(false);
        JPanel wrapper = new JPanel(new BorderLayout());     // keeps the cards at the top, not stretched
        wrapper.setOpaque(false);
        wrapper.add(cardGrid, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    public void refreshData() {
        cardGrid.removeAll();
        try {
            List<Movie> movies = movieService.getAllMovies();
            for (Movie movie : movies) {
                cardGrid.add(buildCard(movie));
            }
            if (movies.isEmpty()) {
                cardGrid.add(Theme.label("No movies are available right now.", Theme.FONT_NORMAL, Theme.TEXT_MUTED));
            }
        } catch (DatabaseException e) {
            Theme.showError(this, e.getMessage());
        }
        cardGrid.revalidate();
        cardGrid.repaint();
    }

    private JPanel buildCard(Movie movie) {
        JPanel card = Theme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(Theme.ACCENT);
        banner.setBorder(BorderFactory.createEmptyBorder(26, 10, 26, 10));
        JLabel rating = Theme.label("Rating " + movie.getRating() + " / 10", Theme.FONT_BOLD, Color.WHITE);
        banner.add(rating, BorderLayout.CENTER);
        banner.setAlignmentX(LEFT_ALIGNMENT);
        banner.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 80));

        JLabel title = Theme.label(movie.getTitle(), Theme.FONT_HEADING, Theme.TEXT_DARK);
        JLabel line1 = Theme.label(movie.getGenre() + "  |  " + movie.getLanguage(), Theme.FONT_NORMAL, Theme.TEXT_MUTED);
        JLabel line2 = Theme.label(movie.getDurationMinutes() + " minutes", Theme.FONT_NORMAL, Theme.TEXT_MUTED);
        JButton details = Theme.button("View Details & Book", Theme.ACCENT);
        details.setAlignmentX(LEFT_ALIGNMENT);
        details.addActionListener(e -> new MovieDetailsDialog(
                SwingUtilities.getWindowAncestor(this), movie, user, onBooked).setVisible(true));

        title.setAlignmentX(LEFT_ALIGNMENT);
        line1.setAlignmentX(LEFT_ALIGNMENT);
        line2.setAlignmentX(LEFT_ALIGNMENT);

        card.add(banner);
        card.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(0, 10)));
        card.add(title);
        card.add(line1);
        card.add(line2);
        card.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(0, 12)));
        card.add(details);
        return card;
    }
}
