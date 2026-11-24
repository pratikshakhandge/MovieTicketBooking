package service;

import dao.MovieDAO;
import exception.DatabaseException;
import exception.InvalidInputException;
import model.Movie;

import java.util.List;

/** Business rules for movies (validation happens here, SQL happens in MovieDAO). */
public class MovieService {

    private final MovieDAO movieDAO = new MovieDAO();

    public List<Movie> getAllMovies() throws DatabaseException {
        return movieDAO.getAllMovies();
    }

    public void addMovie(String title, String genre, String language,
                         String duration, String rating, String description)
            throws InvalidInputException, DatabaseException {
        Movie movie = buildMovie(title, genre, language, duration, rating, description);
        movieDAO.insert(movie);
    }

    public void updateMovie(int movieId, String title, String genre, String language,
                            String duration, String rating, String description)
            throws InvalidInputException, DatabaseException {
        Movie movie = buildMovie(title, genre, language, duration, rating, description);
        movie.setMovieId(movieId);
        if (!movieDAO.update(movie)) {
            throw new InvalidInputException("Movie with ID " + movieId + " was not found.");
        }
    }

    public void deleteMovie(int movieId) throws InvalidInputException, DatabaseException {
        if (!movieDAO.delete(movieId)) {
            throw new InvalidInputException("Movie with ID " + movieId + " was not found.");
        }
    }

    /** Validates all fields and creates a Movie object. */
    private Movie buildMovie(String title, String genre, String language,
                             String duration, String rating, String description)
            throws InvalidInputException {
        String cleanTitle = Validator.requireText(title, "Title");
        String cleanGenre = Validator.requireText(genre, "Genre");
        String cleanLanguage = Validator.requireText(language, "Language");
        int minutes = Validator.parsePositiveInt(duration, "Duration");
        double movieRating = Validator.parseRating(rating);
        String cleanDescription = description == null ? "" : description.trim();
        if (cleanDescription.length() > 500) {
            throw new InvalidInputException("Description can have at most 500 characters.");
        }
        return new Movie(cleanTitle, cleanGenre, cleanLanguage, minutes, movieRating, cleanDescription);
    }
}
