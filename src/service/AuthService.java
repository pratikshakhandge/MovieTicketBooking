package service;

import dao.UserDAO;
import exception.DatabaseException;
import exception.DuplicateUserException;
import exception.InvalidInputException;
import exception.InvalidLoginException;
import model.User;

/** Business rules for registration and login. */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    /** Registers a new CUSTOMER account. */
    public User register(String fullName, String email, String password, String confirmPassword)
            throws InvalidInputException, DuplicateUserException, DatabaseException {
        return register(fullName, email, password, confirmPassword, User.ROLE_CUSTOMER);
    }

    /** Registers a new account with the selected supported role. */
    public User register(String fullName, String email, String password, String confirmPassword, String role)
            throws InvalidInputException, DuplicateUserException, DatabaseException {

        String name = Validator.requireText(fullName, "Full name");
        if (name.length() < 2) {
            throw new InvalidInputException("Full name must have at least 2 characters.");
        }
        String cleanEmail = Validator.requireEmail(email);
        String pass = Validator.requireText(password, "Password");
        if (pass.length() < 4) {
            throw new InvalidInputException("Password must have at least 4 characters.");
        }
        if (!pass.equals(confirmPassword)) {
            throw new InvalidInputException("Password and confirm password do not match.");
        }
        if (userDAO.findByEmail(cleanEmail) != null) {
            throw new DuplicateUserException("An account with this email already exists. Please login.");
        }

        String cleanRole = Validator.requireText(role, "Account type").toUpperCase();
        if (!User.ROLE_ADMIN.equals(cleanRole) && !User.ROLE_CUSTOMER.equals(cleanRole)) {
            throw new InvalidInputException("Please select Admin or Customer.");
        }

        User user = new User(name, cleanEmail, pass, cleanRole);
        user.setUserId(userDAO.insert(user));
        return user;
    }

    /** Checks email and password and returns the logged-in user (admin or customer). */
    public User login(String email, String password)
            throws InvalidInputException, InvalidLoginException, DatabaseException {

        String cleanEmail = Validator.requireText(email, "Email").toLowerCase();
        String pass = Validator.requireText(password, "Password");

        User user = userDAO.findByEmail(cleanEmail);
        if (user == null || !user.getPassword().equals(pass)) {
            throw new InvalidLoginException("Invalid email or password.");
        }
        return user;
    }
}
