package analytics;

import model.Booking;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Data analytics using Java Streams.
 * Every method receives a list of bookings (loaded from MySQL by BookingDAO)
 * and calculates a result using filter(), map(), sorted(), collect(), count(),
 * sum(), groupingBy() and max().
 * Cancelled bookings are ignored in all calculations.
 */
public class BookingAnalytics {

    /** filter() + collect(): keep only confirmed bookings. */
    public List<Booking> confirmedOnly(List<Booking> bookings) {
        return bookings.stream()
                .filter(Booking::isConfirmed)
                .collect(Collectors.toList());
    }

    /** filter() + count(): total number of confirmed bookings. */
    public long totalBookings(List<Booking> bookings) {
        return bookings.stream()
                .filter(Booking::isConfirmed)
                .count();
    }

    /** filter() + mapToDouble() + sum(): total money earned. */
    public double totalRevenue(List<Booking> bookings) {
        return bookings.stream()
                .filter(Booking::isConfirmed)
                .mapToDouble(Booking::getTotalAmount)
                .sum();
    }

    /** filter() + mapToInt() + sum(): total number of tickets (seats) sold. */
    public int totalTicketsSold(List<Booking> bookings) {
        return bookings.stream()
                .filter(Booking::isConfirmed)
                .mapToInt(Booking::getSeatCount)
                .sum();
    }

    /** Average price of one ticket = total revenue / tickets sold. */
    public double averageTicketPrice(List<Booking> bookings) {
        int tickets = totalTicketsSold(bookings);
        return tickets == 0 ? 0 : totalRevenue(bookings) / tickets;
    }

    /** groupingBy() + counting(): number of bookings for each movie. */
    public Map<String, Long> bookingsPerMovie(List<Booking> bookings) {
        return bookings.stream()
                .filter(Booking::isConfirmed)
                .collect(Collectors.groupingBy(Booking::getMovieTitle, Collectors.counting()));
    }

    /** groupingBy() + summingDouble(): revenue of each movie. */
    public Map<String, Double> revenuePerMovie(List<Booking> bookings) {
        return bookings.stream()
                .filter(Booking::isConfirmed)
                .collect(Collectors.groupingBy(Booking::getMovieTitle,
                        Collectors.summingDouble(Booking::getTotalAmount)));
    }

    /** max(): the movie with the highest number of bookings. */
    public Optional<Map.Entry<String, Long>> mostPopularMovie(List<Booking> bookings) {
        return bookingsPerMovie(bookings).entrySet().stream()
                .max((a, b) -> {
                    int byCount = Long.compare(a.getValue(), b.getValue());
                    // if two movies have the same count, the one that comes first alphabetically wins
                    return byCount != 0 ? byCount : b.getKey().compareTo(a.getKey());
                });
    }

    /** sorted() + collect(): movies from most booked to least booked. */
    public List<Map.Entry<String, Long>> moviesSortedByBookings(List<Booking> bookings) {
        return bookingsPerMovie(bookings).entrySet().stream()
                .sorted((a, b) -> {
                    int byCount = Long.compare(b.getValue(), a.getValue());
                    return byCount != 0 ? byCount : a.getKey().compareTo(b.getKey());
                })
                .collect(Collectors.toList());
    }

    /** filter(): bookings made between two dates (both dates included). */
    public List<Booking> filterByDate(List<Booking> bookings, LocalDate from, LocalDate to) {
        return bookings.stream()
                .filter(b -> !b.getBookingDate().toLocalDate().isBefore(from))
                .filter(b -> !b.getBookingDate().toLocalDate().isAfter(to))
                .collect(Collectors.toList());
    }

    /** map() + sorted(): one MovieStat line per movie, most popular first. */
    public List<MovieStat> movieStats(List<Booking> bookings) {
        Map<String, Double> revenue = revenuePerMovie(bookings);
        return bookingsPerMovie(bookings).entrySet().stream()
                .map(entry -> new MovieStat(entry.getKey(), entry.getValue(),
                        revenue.getOrDefault(entry.getKey(), 0.0)))
                .sorted((a, b) -> {
                    int byBookings = Long.compare(b.bookings(), a.bookings());
                    return byBookings != 0 ? byBookings : a.movieTitle().compareTo(b.movieTitle());
                })
                .collect(Collectors.toList());
    }
}
