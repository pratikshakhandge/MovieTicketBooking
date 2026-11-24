package service;

import dao.MovieDAO;
import dao.SeatDAO;
import dao.ShowDAO;
import exception.DatabaseException;
import exception.InvalidInputException;
import exception.InvalidShowException;
import model.Seat;
import model.Show;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Business rules for showtimes and seats. */
public class ShowService {

    private final ShowDAO showDAO = new ShowDAO();
    private final SeatDAO seatDAO = new SeatDAO();
    private final MovieDAO movieDAO = new MovieDAO();

    public List<Show> getAllShows() throws DatabaseException {
        return showDAO.getAllShows();
    }

    /** Only shows that have not started yet, earliest first (uses a Stream). */
    public List<Show> getUpcomingShowsForMovie(int movieId) throws DatabaseException {
        LocalDateTime now = LocalDateTime.now();
        return showDAO.getShowsByMovie(movieId).stream()
                .filter(show -> show.getShowDateTime().isAfter(now))
                .sorted(Comparator.comparing(Show::getShowDateTime))
                .collect(Collectors.toList());
    }

    public Show getShow(int showId) throws InvalidShowException, DatabaseException {
        Show show = showDAO.getShowById(showId);
        if (show == null) {
            throw new InvalidShowException("Show with ID " + showId + " does not exist.");
        }
        return show;
    }

    public List<Seat> getSeats(int showId) throws DatabaseException {
        return seatDAO.getSeatsByShow(showId);
    }

    /** Admin adds a showtime. All 40 seats are created automatically. */
    public int addShow(int movieId, String dateText, String timeText, String screen, String priceText)
            throws InvalidInputException, DatabaseException {

        if (movieDAO.getMovieById(movieId) == null) {
            throw new InvalidInputException("Please select a valid movie.");
        }
        LocalDate date = Validator.parseDate(dateText, "Show date");
        LocalTime time = Validator.parseTime(timeText, "Show time");
        String screenName = Validator.requireText(screen, "Screen");
        double price = Validator.parsePositiveDouble(priceText, "Ticket price");

        if (!LocalDateTime.of(date, time).isAfter(LocalDateTime.now())) {
            throw new InvalidInputException("Show date and time must be in the future.");
        }

        Show show = new Show();
        show.setMovieId(movieId);
        show.setShowDate(date);
        show.setShowTime(time);
        show.setScreenName(screenName);
        show.setTicketPrice(price);
        return showDAO.insertShow(show);
    }
}
