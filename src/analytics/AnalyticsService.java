package analytics;

import dao.BookingDAO;
import dao.ShowDAO;
import exception.DatabaseException;
import exception.InvalidInputException;
import model.Booking;
import model.Show;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Connects the database (DAO classes) with the Streams calculations.
 * Step 1: fetch real data through DAO.  Step 2: analyse it with BookingAnalytics.
 */
public class AnalyticsService {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final ShowDAO showDAO = new ShowDAO();
    private final BookingAnalytics analytics = new BookingAnalytics();

    /** Loads everything the admin dashboard needs. */
    public DashboardStats loadDashboardStats() throws DatabaseException {
        List<Booking> bookings = bookingDAO.getAllBookings();
        List<Show> shows = showDAO.getAllShows();

        String popular = analytics.mostPopularMovie(bookings)
                .map(Map.Entry::getKey)
                .orElse("No bookings yet");

        int seatsTotal = shows.stream().mapToInt(Show::getTotalSeats).sum();
        int seatsBooked = shows.stream().mapToInt(Show::getBookedSeats).sum();

        return new DashboardStats(
                analytics.totalBookings(bookings),
                analytics.totalRevenue(bookings),
                analytics.totalTicketsSold(bookings),
                analytics.averageTicketPrice(bookings),
                popular,
                seatsBooked,
                seatsTotal,
                analytics.movieStats(bookings));
    }

    /** Confirmed bookings made between two dates. */
    public List<Booking> getBookingsBetween(LocalDate from, LocalDate to)
            throws DatabaseException, InvalidInputException {
        if (from.isAfter(to)) {
            throw new InvalidInputException("'From' date must not be after 'To' date.");
        }
        List<Booking> all = bookingDAO.getAllBookings();
        return analytics.confirmedOnly(analytics.filterByDate(all, from, to));
    }

    public double revenueOf(List<Booking> bookings) {
        return analytics.totalRevenue(bookings);
    }
}
