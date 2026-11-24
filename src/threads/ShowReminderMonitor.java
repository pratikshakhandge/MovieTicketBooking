package threads;

import exception.DatabaseException;
import model.Booking;
import service.BookingService;

import javax.swing.SwingUtilities;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * BACKGROUND THREAD 2: notification / reminder monitor for customers.
 * Every 30 seconds it checks the database for the customer's confirmed bookings
 * whose show starts within the next few hours and sends a reminder message to the screen.
 */
public class ShowReminderMonitor {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final ScheduledExecutorService scheduler;
    private final BookingService bookingService;
    private final int userId;
    private final int hoursAhead;
    private final Consumer<String> onMessage;

    public ShowReminderMonitor(BookingService bookingService, int userId, int hoursAhead,
                               Consumer<String> onMessage) {
        this.bookingService = bookingService;
        this.userId = userId;
        this.hoursAhead = hoursAhead;
        this.onMessage = onMessage;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "show-reminder-monitor");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void start() {
        scheduler.scheduleWithFixedDelay(this::checkOnce, 0, 30, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    private void checkOnce() {
        // runs on the BACKGROUND thread; never throws
        String message;
        try {
            List<Booking> upcoming = bookingService.getUpcomingBookings(userId, hoursAhead);
            if (upcoming.isEmpty()) {
                message = "No shows in the next " + hoursAhead + " hours.";
            } else {
                Booking next = upcoming.get(0);
                message = "Reminder: \"" + next.getMovieTitle() + "\" starts on "
                        + next.getShowDate().format(DATE_FORMAT) + " at " + next.getShowTime()
                        + " (" + next.getScreenName() + ", seats " + next.getSeatNumbers() + ")"
                        + (upcoming.size() > 1 ? "  +" + (upcoming.size() - 1) + " more upcoming" : "");
            }
        } catch (DatabaseException e) {
            message = "Reminder check failed: " + e.getMessage();
        } catch (RuntimeException e) {
            message = "Reminder check failed: " + e.getMessage();
        }
        final String finalMessage = message;
        SwingUtilities.invokeLater(() -> onMessage.accept(finalMessage));   // update label on GUI thread
    }
}
