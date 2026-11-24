package ui;

import model.User;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Main window for the admin: sidebar navigation + CardLayout with four screens. */
public class AdminDashboard extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);

    private final AdminMoviesPanel moviesPanel = new AdminMoviesPanel();
    private final AdminShowsPanel showsPanel = new AdminShowsPanel();
    private final AdminBookingsPanel bookingsPanel = new AdminBookingsPanel();
    private final AdminStatsPanel statsPanel = new AdminStatsPanel();

    public AdminDashboard(User admin) {
        super("Admin Dashboard - " + admin.getFullName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        content.add(moviesPanel, "movies");
        content.add(showsPanel, "shows");
        content.add(bookingsPanel, "bookings");
        content.add(statsPanel, "stats");

        add(buildSidebar(admin), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                statsPanel.stop();                       // stop the background thread
            }
        });

        moviesPanel.refreshData();
        cardLayout.show(content, "movies");
    }

    private JPanel buildSidebar(User admin) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 16, 24, 16));
        sidebar.setPreferredSize(new Dimension(220, 0));

        sidebar.add(Theme.label("ADMIN PANEL", Theme.FONT_HEADING, Color.WHITE));
        sidebar.add(javax.swing.Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(Theme.label(admin.getFullName(), Theme.FONT_NORMAL, new Color(203, 213, 225)));
        sidebar.add(javax.swing.Box.createRigidArea(new Dimension(0, 28)));

        JButton movies = navButton("Movies");
        JButton shows = navButton("Showtimes & Seats");
        JButton bookings = navButton("All Bookings");
        JButton stats = navButton("Statistics");
        JButton logout = navButton("Logout");
        logout.setBackground(Theme.DANGER);

        movies.addActionListener(e -> {
            moviesPanel.refreshData();
            cardLayout.show(content, "movies");
        });
        shows.addActionListener(e -> {
            showsPanel.refreshData();
            cardLayout.show(content, "shows");
        });
        bookings.addActionListener(e -> {
            bookingsPanel.refreshData();
            cardLayout.show(content, "bookings");
        });
        stats.addActionListener(e -> {
            statsPanel.refreshData();
            cardLayout.show(content, "stats");
        });
        logout.addActionListener(e -> {
            statsPanel.stop();
            dispose();
            new LoginFrame().setVisible(true);
        });

        sidebar.add(movies);
        sidebar.add(javax.swing.Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(shows);
        sidebar.add(javax.swing.Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(bookings);
        sidebar.add(javax.swing.Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(stats);
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
}
