package model;

/** Represents one row of the 'seats' table (one seat of one show). */
public class Seat {

    /** Seat layout used everywhere: rows A-E, columns 1-8 = 40 seats. */
    public static final String[] ROWS = {"A", "B", "C", "D", "E"};
    public static final int COLUMNS = 8;
    public static final int TOTAL_SEATS = ROWS.length * COLUMNS;

    private int seatId;
    private int showId;
    private String seatNumber;
    private boolean booked;

    public Seat() {
    }

    public Seat(int seatId, int showId, String seatNumber, boolean booked) {
        this.seatId = seatId;
        this.showId = showId;
        this.seatNumber = seatNumber;
        this.booked = booked;
    }

    public int getSeatId() { return seatId; }
    public void setSeatId(int seatId) { this.seatId = seatId; }
    public int getShowId() { return showId; }
    public void setShowId(int showId) { this.showId = showId; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public boolean isBooked() { return booked; }
    public void setBooked(boolean booked) { this.booked = booked; }
}
