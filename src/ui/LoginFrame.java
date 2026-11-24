package ui;

import exception.DatabaseException;
import exception.DuplicateUserException;
import exception.InvalidInputException;
import exception.InvalidLoginException;
import model.User;
import service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/** First screen: Login and Registration (two cards inside one window). */
public class LoginFrame extends JFrame {

    private static final String CARD_LOGIN = "login";
    private static final String CARD_REGISTER = "register";

    private final AuthService authService = new AuthService();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel formCards = new JPanel(cardLayout);

    private final JTextField loginEmail = Theme.inputField(20);
    private final JPasswordField loginPassword = Theme.passwordField(20);
    private final JTextField regName = Theme.inputField(20);
    private final JTextField regEmail = Theme.inputField(20);
    private final JPasswordField regPassword = Theme.passwordField(20);
    private final JPasswordField regConfirm = Theme.passwordField(20);
    private final JButton loginButton = Theme.button("Login", Theme.ACCENT);
    private final JButton registerButton = Theme.button("Register", Theme.SUCCESS);

    public LoginFrame() {
        super("Movie Ticket Booking System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new GridLayout(1, 2));

        formCards.setBackground(Theme.BACKGROUND);
        formCards.add(buildLoginPanel(), CARD_LOGIN);
        formCards.add(buildRegisterPanel(), CARD_REGISTER);

        add(buildBrandPanel());
        add(formCards);
        showCard(CARD_LOGIN);
    }

    // ---------- left side: brand ----------

    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Theme.ACCENT);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(150, 40, 40, 40));

        JLabel name = Theme.label("CINEMA", new java.awt.Font("SansSerif", java.awt.Font.BOLD, 44), Color.WHITE);
        JLabel name2 = Theme.label("BOOKING", new java.awt.Font("SansSerif", java.awt.Font.BOLD, 44), Color.WHITE);
        JLabel line1 = Theme.label("Movie Ticket Booking", Theme.FONT_HEADING, new Color(224, 231, 255));
        JLabel line2 = Theme.label("Management System", Theme.FONT_HEADING, new Color(224, 231, 255));
        JLabel hint = Theme.label("Book your seats in a few clicks.", Theme.FONT_NORMAL, new Color(199, 210, 254));

        panel.add(name);
        panel.add(name2);
        panel.add(javax.swing.Box.createRigidArea(new Dimension(0, 18)));
        panel.add(line1);
        panel.add(line2);
        panel.add(javax.swing.Box.createRigidArea(new Dimension(0, 30)));
        panel.add(hint);
        return panel;
    }

    // ---------- right side: login card ----------

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BACKGROUND);

        int row = 0;
        addFull(panel, Theme.title("Welcome back"), row++, 0, 4);
        addFull(panel, Theme.label("Login to continue", Theme.FONT_NORMAL, Theme.TEXT_MUTED), row++, 0, 18);
        addRow(panel, "Email", loginEmail, row++);
        addRow(panel, "Password", loginPassword, row++);

        addFull(panel, loginButton, row++, 12, 6);
        JButton toRegister = Theme.button("New customer? Create an account", Theme.SIDEBAR);
        addFull(panel, toRegister, row++, 4, 6);
        addFull(panel, Theme.label("Demo admin: admin@movie.com / admin123", Theme.FONT_NORMAL, Theme.TEXT_MUTED),
                row, 10, 0);

        loginButton.addActionListener(e -> handleLogin());
        toRegister.addActionListener(e -> showCard(CARD_REGISTER));
        return panel;
    }

    // ---------- right side: register card ----------

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BACKGROUND);

        int row = 0;
        addFull(panel, Theme.title("Create account"), row++, 0, 4);
        addFull(panel, Theme.label("Customer registration", Theme.FONT_NORMAL, Theme.TEXT_MUTED), row++, 0, 14);
        addRow(panel, "Full name", regName, row++);
        addRow(panel, "Email", regEmail, row++);
        addRow(panel, "Password", regPassword, row++);
        addRow(panel, "Confirm password", regConfirm, row++);

        addFull(panel, registerButton, row++, 12, 6);
        JButton back = Theme.button("Back to login", Theme.SIDEBAR);
        addFull(panel, back, row, 4, 0);

        registerButton.addActionListener(e -> handleRegister());
        back.addActionListener(e -> showCard(CARD_LOGIN));
        return panel;
    }

    // ---------- layout helpers ----------

    private void addRow(JPanel panel, String labelText, javax.swing.JComponent field, int row) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(6, 0, 6, 12);
        panel.add(Theme.formLabel(labelText), c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = row;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 0, 6, 0);
        panel.add(field, c);
    }

    private void addFull(JPanel panel, javax.swing.JComponent component, int row, int top, int bottom) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(top, 0, bottom, 0);
        panel.add(component, c);
    }

    private void showCard(String name) {
        cardLayout.show(formCards, name);
        // pressing Enter triggers the button of the visible card
        getRootPane().setDefaultButton(CARD_LOGIN.equals(name) ? loginButton : registerButton);
    }

    // ---------- actions ----------

    private void handleLogin() {
        try {
            User user = authService.login(loginEmail.getText(), new String(loginPassword.getPassword()));
            openDashboard(user);
        } catch (InvalidInputException | InvalidLoginException | DatabaseException e) {
            Theme.showError(this, e.getMessage());
        }
    }

    private void handleRegister() {
        try {
            User user = authService.register(regName.getText(), regEmail.getText(),
                    new String(regPassword.getPassword()), new String(regConfirm.getPassword()));
            Theme.showInfo(this, "Registration successful! You can login now.");
            loginEmail.setText(user.getEmail());
            loginPassword.setText("");
            regName.setText("");
            regEmail.setText("");
            regPassword.setText("");
            regConfirm.setText("");
            showCard(CARD_LOGIN);
        } catch (InvalidInputException | DuplicateUserException | DatabaseException e) {
            Theme.showError(this, e.getMessage());
        }
    }

    /** Role-based access: admins and customers see different dashboards. */
    private void openDashboard(User user) {
        dispose();
        if (user.isAdmin()) {
            new AdminDashboard(user).setVisible(true);
        } else {
            new CustomerDashboard(user).setVisible(true);
        }
    }
}
