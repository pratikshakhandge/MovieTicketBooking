package analytics;

/** One line of the "popular movies" report. A record is a short way to write a data-only class. */
public record MovieStat(String movieTitle, long bookings, double revenue) {
}
