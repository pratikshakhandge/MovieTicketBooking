package ui;

import exception.DatabaseException;
import exception.InvalidInputException;
import model.Movie;
import service.MovieService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/** Admin screen: add, update, delete and view movies. */
public class AdminMoviesPanel extends JPanel {

    private final MovieService movieService = new MovieService();
    private final DefaultTableModel model =
            Theme.readOnlyModel("ID", "Title", "Genre", "Language", "Duration (min)", "Rating");
    private final JTable table = new JTable(model);
    private List<Movie> movies = new ArrayList<>();
    private int selectedMovieId = -1;

    private final JTextField titleField = Theme.inputField(18);
    private final JTextField genreField = Theme.inputField(12);
    private final JTextField languageField = Theme.inputField(12);
    private final JTextField durationField = Theme.inputField(8);
    private final JTextField ratingField = Theme.inputField(8);
    private final JTextArea descriptionArea = new JTextArea(3, 40);

    public AdminMoviesPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(Theme.title("Manage Movies"), BorderLayout.NORTH);
        add(Theme.scroll(table), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelection();
            }
        });
    }

    private JPanel buildForm() {
        JPanel form = Theme.card();
        form.setLayout(new GridBagLayout());

        descriptionArea.setFont(Theme.FONT_NORMAL);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        add(form, "Title", titleField, 0, 0, 3);
        add(form, "Genre", genreField, 1, 0, 1);
        add(form, "Language", languageField, 1, 1, 1);
        add(form, "Duration (min)", durationField, 2, 0, 1);
        add(form, "Rating (0-10)", ratingField, 2, 1, 1);
        add(form, "Description", descScroll, 3, 0, 3);

        JButton addButton = Theme.button("Add Movie", Theme.SUCCESS);
        JButton updateButton = Theme.button("Update Selected", Theme.ACCENT);
        JButton deleteButton = Theme.button("Delete Selected", Theme.DANGER);
        JButton clearButton = Theme.button("Clear Form", Theme.SIDEBAR);
        addButton.addActionListener(e -> addMovie());
        updateButton.addActionListener(e -> updateMovie());
        deleteButton.addActionListener(e -> deleteMovie());
        clearButton.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 6;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(10, 0, 0, 0);
        form.add(buttons, c);
        return form;
    }

    /** Adds a label + component pair. 'span' = how many label/field pairs wide the field is. */
    private void add(JPanel form, String label, javax.swing.JComponent field, int row, int pairIndex, int span) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = pairIndex * 2;
        c.gridy = row;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 0, 4, 8);
        form.add(Theme.formLabel(label), c);

        c = new GridBagConstraints();
        c.gridx = pairIndex * 2 + 1;
        c.gridy = row;
        c.gridwidth = span == 3 ? 5 : 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(4, 0, 4, 16);
        form.add(field, c);
    }

    public void refreshData() {
        model.setRowCount(0);
        try {
            movies = movieService.getAllMovies();
        } catch (DatabaseException e) {
            Theme.showError(this, e.getMessage());
            movies = new ArrayList<>();
        }
        for (Movie m : movies) {
            model.addRow(new Object[]{m.getMovieId(), m.getTitle(), m.getGenre(), m.getLanguage(),
                    m.getDurationMinutes(), m.getRating()});
        }
        selectedMovieId = -1;
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= movies.size()) {
            return;
        }
        Movie m = movies.get(row);
        selectedMovieId = m.getMovieId();
        titleField.setText(m.getTitle());
        genreField.setText(m.getGenre());
        languageField.setText(m.getLanguage());
        durationField.setText(String.valueOf(m.getDurationMinutes()));
        ratingField.setText(String.valueOf(m.getRating()));
        descriptionArea.setText(m.getDescription());
    }

    private void clearForm() {
        table.clearSelection();
        selectedMovieId = -1;
        titleField.setText("");
        genreField.setText("");
        languageField.setText("");
        durationField.setText("");
        ratingField.setText("");
        descriptionArea.setText("");
    }

    private void addMovie() {
        try {
            movieService.addMovie(titleField.getText(), genreField.getText(), languageField.getText(),
                    durationField.getText(), ratingField.getText(), descriptionArea.getText());
            Theme.showInfo(this, "Movie added successfully.");
            clearForm();
            refreshData();
        } catch (InvalidInputException | DatabaseException e) {
            Theme.showError(this, e.getMessage());
        }
    }

    private void updateMovie() {
        if (selectedMovieId < 0) {
            Theme.showInfo(this, "Please select a movie from the table to update.");
            return;
        }
        try {
            movieService.updateMovie(selectedMovieId, titleField.getText(), genreField.getText(),
                    languageField.getText(), durationField.getText(), ratingField.getText(),
                    descriptionArea.getText());
            Theme.showInfo(this, "Movie updated successfully.");
            clearForm();
            refreshData();
        } catch (InvalidInputException | DatabaseException e) {
            Theme.showError(this, e.getMessage());
        }
    }

    private void deleteMovie() {
        if (selectedMovieId < 0) {
            Theme.showInfo(this, "Please select a movie from the table to delete.");
            return;
        }
        if (!Theme.confirm(this, "Delete \"" + titleField.getText() + "\" and all its showtimes?")) {
            return;
        }
        try {
            movieService.deleteMovie(selectedMovieId);
            Theme.showInfo(this, "Movie deleted.");
            clearForm();
            refreshData();
        } catch (InvalidInputException | DatabaseException e) {
            Theme.showError(this, e.getMessage());
        }
    }
}
