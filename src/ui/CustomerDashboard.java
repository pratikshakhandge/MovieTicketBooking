package ui;

import model.User;
import service.BookingService;
import threads.ShowReminderMonitor;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Main window for customers: sidebar navigation + CardLayout content + reminder bar. */
public class CustomerDashboard extends JFrame {

    private static final String CARD_MOVIES = "movies";
    private static final String CARD_BOOKINGS = "bookings";
    private static final int REMINDER_HOURS = 48;

    private final User user;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final CustomerBookingsPanel bookingsPanel;
    private final CustomerMoviesPanel moviesPanel;
    private final JLabel reminderLabel = Theme.label("Checking for upcoming shows...", Theme.FONT_NORMAL, Color.WHITE);
    private final ShowReminderMonitor reminderMonitor;

    public CustomerDashboard(User user) {
        super("Customer Dashboard - " + user.getFullName());
        this.user = user;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        bookingsPanel = new CustomerBookingsPanel(user);
        moviesPanel = new CustomerMoviesPanel(user, bookingsPanel::refreshData);
        content.add(moviesPanel, CARD_MOVIES);
        content.add(bookingsPanel, CARD_BOOKINGS);

        add(buildSidebar(), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        add(buildReminderBar(), BorderLayout.SOUTH);

        // background thread: reminders about upcoming shows
        reminderMonitor = new ShowReminderMonitor(new BookingService(), user.getUserId(),
                REMINDER_HOURS, reminderLabel::setText);
        reminderMonitor.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                reminderMonitor.stop();
            }
        });

        showMovies();
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 16, 24, 16));
        sidebar.setPreferredSize(new Dimension(220, 0));

        sidebar.add(Theme.label("CINEMA BOOKING", Theme.FONT_HEADING, Color.WHITE));
        sidebar.add(javax.swing.Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(Theme.label("Hello, " + user.getFullName(), Theme.FONT_NORMAL, new Color(203, 213, 225)));
        sidebar.add(javax.swing.Box.createRigidArea(new Dimension(0, 28)));

        JButton movies = navButton("Browse Movies");
        JButton bookings = navButton("My Bookings");
        JButton logout = navButton("Logout");
        logout.setBackground(Theme.DANGER);

        movies.addActionListener(e -> showMovies());
        bookings.addActionListener(e -> showBookings());
        logout.addActionListener(e -> logout());

        sidebar.add(movies);
        sidebar.add(javax.swing.Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(bookings);
        sidebar.add(javax.swing.Box.createVerticalGlue());
        sidebar.add(logout);
        return sidebar;
    }

    private JButton navButton(String text) {
        JButton button = Theme.button(text, Theme.ACCENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setAlignmentX(LEFT_ALIGNMENT);
        return button;
    }

    private JPanel buildReminderBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.WARNING);
        bar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        bar.add(reminderLabel, BorderLayout.CENTER);
        return bar;
    }

    private void showMovies() {
        moviesPanel.refreshData();
        cardLayout.show(content, CARD_MOVIES);
    }

    private void showBookings() {
        bookingsPanel.refreshData();
        cardLayout.show(content, CARD_BOOKINGS);
    }

    private void logout() {
        reminderMonitor.stop();
        dispose();
        new LoginFrame().setVisible(true);
    }
}
