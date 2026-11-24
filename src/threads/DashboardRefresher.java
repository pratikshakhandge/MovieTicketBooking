package threads;

import analytics.AnalyticsService;
import analytics.DashboardStats;
import exception.DatabaseException;

import javax.swing.SwingUtilities;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * BACKGROUND THREAD 1: refreshes the admin statistics automatically.
 *
 * - A ScheduledExecutorService runs refreshOnce() every few seconds on its own thread.
 * - The slow database work happens on that background thread, so the screen never freezes.
 * - The result is given to the screen with SwingUtilities.invokeLater(), because Swing
 *   components may only be changed on the Event Dispatch Thread (the GUI thread).
 */
public class DashboardRefresher {

    private final ScheduledExecutorService scheduler;
    private final AnalyticsService analyticsService;
    private final Consumer<DashboardStats> onSuccess;
    private final Consumer<String> onError;
    private final int periodSeconds;

    public DashboardRefresher(AnalyticsService analyticsService, int periodSeconds,
                              Consumer<DashboardStats> onSuccess, Consumer<String> onError) {
        this.analyticsService = analyticsService;
        this.periodSeconds = periodSeconds;
        this.onSuccess = onSuccess;
        this.onError = onError;
        // daemon thread = does not stop the program from closing
        this.scheduler = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "dashboard-refresher");
            thread.setDaemon(true);
            return thread;
        });
    }

    /** Starts refreshing: first run after 'periodSeconds', then repeats. */
    public void start() {
        scheduler.scheduleWithFixedDelay(this::refreshOnce, periodSeconds, periodSeconds, TimeUnit.SECONDS);
    }

    /** Stops the background thread (called when the admin window closes). */
    public void stop() {
        scheduler.shutdownNow();
    }

    private void refreshOnce() {
        // This method runs on the BACKGROUND thread.
        // It must never throw an exception, otherwise the scheduler stops repeating it.
        try {
            DashboardStats stats = analyticsService.loadDashboardStats();
            SwingUtilities.invokeLater(() -> onSuccess.accept(stats));      // back to the GUI thread
        } catch (DatabaseException e) {
            SwingUtilities.invokeLater(() -> onError.accept(e.getMessage()));
        } catch (RuntimeException e) {
            SwingUtilities.invokeLater(() -> onError.accept("Unexpected error: " + e.getMessage()));
        }
    }
}
