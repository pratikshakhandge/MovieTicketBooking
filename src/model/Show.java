package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Represents one row of the 'shows' table (a showtime of a movie). */
public class Show {

    private int showId;
    private int movieId;
    private String movieTitle;     // filled by a JOIN with the movies table
    private LocalDate showDate;
    private LocalTime showTime;
    private String screenName;
    private double ticketPrice;
    private int totalSeats;
    private int availableSeats;    // calculated from the seats table

    public Show() {
    }

    public LocalDateTime getShowDateTime() {
        return LocalDateTime.of(showDate, showTime);
    }

    public int getBookedSeats() {
        return totalSeats - availableSeats;
    }

    /** Used in combo boxes and messages. */
    @Override
    public String toString() {
        return showDate + " " + showTime + " | " + screenName + " | Rs. " + String.format("%.2f", ticketPrice);
    }

    public int getShowId() { return showId; }
    public void setShowId(int showId) { this.showId = showId; }
    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public LocalDate getShowDate() { return showDate; }
    public void setShowDate(LocalDate showDate) { this.showDate = showDate; }
    public LocalTime getShowTime() { return showTime; }
    public void setShowTime(LocalTime showTime) { this.showTime = showTime; }
    public String getScreenName() { return screenName; }
    public void setScreenName(String screenName) { this.screenName = screenName; }
    public double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(double ticketPrice) { this.ticketPrice = ticketPrice; }
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
}
