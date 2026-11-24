package analytics;

import java.util.List;

/** All numbers shown on the admin statistics screen. */
public record DashboardStats(
        long totalBookings,
        double totalRevenue,
        int ticketsSold,
        double averageTicketPrice,
        String mostPopularMovie,
        int seatsBooked,
        int seatsTotal,
        List<MovieStat> movieStats) {
}
