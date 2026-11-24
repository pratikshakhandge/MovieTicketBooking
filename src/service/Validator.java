package service;

import exception.InvalidInputException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/** Small helper methods to check and convert user input. Used by all service classes. */
public final class Validator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private Validator() {
    }

    /** Makes sure the text is not empty and returns it trimmed. */
    public static String requireText(String value, String fieldName) throws InvalidInputException {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException(fieldName + " cannot be empty.");
        }
        return value.trim();
    }

    public static String requireEmail(String value) throws InvalidInputException {
        String email = requireText(value, "Email").toLowerCase();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidInputException("Please enter a valid email address (example: name@gmail.com).");
        }
        return email;
    }

    public static int parsePositiveInt(String value, String fieldName) throws InvalidInputException {
        String text = requireText(value, fieldName);
        try {
            int number = Integer.parseInt(text);
            if (number <= 0) {
                throw new InvalidInputException(fieldName + " must be greater than 0.");
            }
            return number;
        } catch (NumberFormatException e) {
            throw new InvalidInputException(fieldName + " must be a whole number.");
        }
    }

    public static double parsePositiveDouble(String value, String fieldName) throws InvalidInputException {
        String text = requireText(value, fieldName);
        try {
            double number = Double.parseDouble(text);
            if (number <= 0) {
                throw new InvalidInputException(fieldName + " must be greater than 0.");
            }
            return number;
        } catch (NumberFormatException e) {
            throw new InvalidInputException(fieldName + " must be a number.");
        }
    }

    /** Movie rating must be between 0 and 10. */
    public static double parseRating(String value) throws InvalidInputException {
        String text = requireText(value, "Rating");
        try {
            double rating = Double.parseDouble(text);
            if (rating < 0 || rating > 10) {
                throw new InvalidInputException("Rating must be between 0 and 10.");
            }
            return rating;
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Rating must be a number (example: 8.5).");
        }
    }

    /** Expects the format yyyy-MM-dd, for example 2026-12-25. */
    public static LocalDate parseDate(String value, String fieldName) throws InvalidInputException {
        String text = requireText(value, fieldName);
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException(fieldName + " must be in the format yyyy-MM-dd (example: 2026-12-25).");
        }
    }

    /** Expects the format HH:mm in 24-hour time, for example 18:30. */
    public static LocalTime parseTime(String value, String fieldName) throws InvalidInputException {
        String text = requireText(value, fieldName);
        try {
            return LocalTime.parse(text);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException(fieldName + " must be in the format HH:mm (example: 18:30).");
        }
    }
}
