# Movie Ticket Booking Management System: Project Report

## 1. Abstract
The Movie Ticket Booking Management System is a desktop application built with Core Java, Java Swing, JDBC and MySQL. It replaces manual ticket booking with a computerised system in which customers can browse movies, choose showtimes, select seats on a visual seat map and book tickets, while the administrator manages movies and showtimes and monitors bookings and revenue. The project demonstrates a packaged, layered architecture (model, DAO, service, UI), database persistence with JDBC and transactions, data analytics with Java Streams, background processing with ExecutorService and ScheduledExecutorService, and professional exception handling.

## 2. Problem Statement
Manual ticket booking causes long queues, double booking of the same seat, calculation mistakes, lost records and no quick way for management to see revenue or popular movies. A reliable computerised system is needed that stores all data permanently, prevents two customers from booking the same seat, handles failures safely and gives management instant statistics.

## 3. Objectives
1. Provide secure role-based login (admin and customer) and customer registration.
2. Let the admin add, update, delete and view movies and add showtimes.
3. Let customers browse movies, view details, select seats and book tickets.
4. Prevent double booking using database transactions with commit and rollback.
5. Allow booking history, ticket view and rule-based cancellation.
6. Generate analytics (total bookings, revenue, popular movies, average ticket price) using Java Streams.
7. Refresh statistics and show reminders automatically using background threads without freezing the GUI.
8. Follow clean coding, packages and proper exception handling.

## 4. Software and Hardware Requirements
**Software:** Java JDK 26, MySQL Server (port 3307), MySQL Workbench, MySQL Connector/J (JDBC driver), VS Code with Extension Pack for Java, Windows 10/11 (also runs on Linux or macOS).
**Hardware:** Any computer with at least 4 GB RAM (8 GB recommended), a dual-core processor, 1 GB of free disk space and a 1280x720 or better display.

## 5. Database Schema Explanation
Database name: `movie_booking_system`

| Table | Purpose | Important columns |
|---|---|---|
| `users` | Admin and customer accounts | `user_id` (PK), `email` (UNIQUE), `role` (ADMIN/CUSTOMER) |
| `movies` | Movie catalogue | `movie_id` (PK), `title`, `genre`, `language`, `duration_minutes`, `rating` (CHECK 0-10) |
| `shows` | Showtimes | `show_id` (PK), `movie_id` (FK), `show_date`, `show_time`, `screen_name`, `ticket_price` |
| `seats` | One row per seat per show | `seat_id` (PK), `show_id` (FK), `seat_number`, `is_booked`, UNIQUE(`show_id`,`seat_number`) |
| `bookings` | One row per booking | `booking_id` (PK), `user_id` (FK), `show_id` (FK), `total_amount`, `status` |
| `booking_seats` | Links a booking to its seats | `booking_id` (FK), `seat_id` (FK), UNIQUE(`booking_id`,`seat_id`) |

Relationships: movies 1-to-many shows; shows 1-to-many seats; users 1-to-many bookings; shows 1-to-many bookings; bookings many-to-many seats through `booking_seats`.
Each show has 40 seats (rows A-E, columns 1-8), created automatically when a show is added.

## 6. System Architecture
The application uses a layered architecture. Each layer talks only to the layer below it.

```
 UI (Swing screens)          package ui
        |
 Service (business rules)    package service
        |                    \
 DAO (SQL, JDBC)              analytics (Streams)   threads (background tasks)
        |
 database (DBConnection)
        |
     MySQL
```
Model classes (`User`, `Movie`, `Show`, `Seat`, `Booking`) travel between all layers. Custom exceptions travel upward from DAO to UI, where they are shown with `JOptionPane`.

### Package and class responsibilities
| Package | Classes | Responsibility |
|---|---|---|
| `main` | `Main` | Sets look and feel, checks the database, opens the login window on the GUI thread |
| `model` | `User`, `Movie`, `Show`, `Seat`, `Booking` | Data holders, one per table |
| `database` | `DBConnection`, `TestConnection` | Creates JDBC connections; tests the setup |
| `dao` | `UserDAO`, `MovieDAO`, `ShowDAO`, `SeatDAO`, `BookingDAO` | All SQL using `PreparedStatement`; transactions for booking, cancelling and adding shows |
| `service` | `Validator`, `AuthService`, `MovieService`, `ShowService`, `BookingService` | Input validation and business rules (max 6 seats, cancellation rule, show must be in the future) |
| `analytics` | `BookingAnalytics`, `AnalyticsService`, `DashboardStats`, `MovieStat` | Java Streams calculations on data fetched through DAOs |
| `threads` | `DashboardRefresher`, `ShowReminderMonitor` | Scheduled background tasks |
| `exception` | `DatabaseException`, `InvalidLoginException`, `DuplicateUserException`, `InvalidInputException`, `SeatAlreadyBookedException`, `NoSeatsAvailableException`, `InvalidShowException`, `BookingCancellationException` | Meaningful error types |
| `ui` | `Theme`, `LoginFrame`, `AdminDashboard`, `AdminMoviesPanel`, `AdminShowsPanel`, `AdminBookingsPanel`, `AdminStatsPanel`, `CustomerDashboard`, `CustomerMoviesPanel`, `CustomerBookingsPanel`, `MovieDetailsDialog`, `SeatSelectionDialog`, `BookingConfirmationDialog` | All Swing screens |

## 7. Module Descriptions
1. **Login and Registration module:** Registration validates name, email format, password length and matching confirmation, and rejects duplicate emails. Login checks the credentials and opens the admin or customer dashboard (role-based access).
2. **Admin Movie Management:** Table of all movies plus a form to add, update and delete movies. Deleting a movie that already has bookings is refused with a clear message.
3. **Admin Showtime and Seat module:** Add a showtime (date, time, screen, price). All 40 seats are created in the same transaction. The admin can view the available and booked seats of any show.
4. **Admin Bookings module:** Lists every booking with customer, movie, seats, amount and status.
5. **Admin Statistics module:** Total bookings, total revenue, tickets sold, average ticket price, most popular movie, popular-movie ranking, seat occupancy and a date filter. Updates automatically every 10 seconds.
6. **Customer Browsing module:** Movies displayed as cards; a details dialog shows the description and upcoming showtimes.
7. **Seat Selection and Booking module:** Swing buttons show seats in three colours (green available, blue selected, red booked). The total price is calculated live. Booking runs inside a database transaction and ends with a confirmation screen showing the booking ID.
8. **Booking History and Cancellation module:** Customers see their bookings, view the ticket and cancel while the rules allow.
9. **Analytics module (Streams):** Described in section 12.
10. **Notification module (threads):** A background thread reminds the customer about shows starting within 48 hours.

## 8. Testing Cases
| # | Test case | Input / action | Expected result | Result |
|---|---|---|---|---|
| 1 | Admin login | admin@movie.com / admin123 | Admin dashboard opens | Pass |
| 2 | Invalid login | Correct email, wrong password | "Invalid email or password." | Pass |
| 3 | Empty login | Empty fields | "Email cannot be empty." | Pass |
| 4 | Customer registration | New valid details | Account created, can login | Pass |
| 5 | Duplicate registration | Already used email | "An account with this email already exists." | Pass |
| 6 | Invalid email | "nope" | Valid-email message | Pass |
| 7 | Password mismatch | Different confirm password | Mismatch message | Pass |
| 8 | Add movie | Valid details | Movie appears in table | Pass |
| 9 | Invalid duration | "abc" | "Duration must be a whole number." | Pass |
| 10 | Invalid rating | 11 | "Rating must be between 0 and 10." | Pass |
| 11 | Update / delete movie | Select and press button | Table updates | Pass |
| 12 | Delete movie with bookings | Movie 1 | Refused with friendly message | Pass |
| 13 | Add show | Future date, time, price | Show added with 40 seats | Pass |
| 14 | Add show in the past | 2020-01-01 | "Show date and time must be in the future." | Pass |
| 15 | Wrong date format | 31/12/2026 | Format message | Pass |
| 16 | Book 3 seats | A1, A2, A3 at Rs. 300 | Total Rs. 900, booking ID shown | Pass |
| 17 | Double booking | Book A3 again | "Seat already booked" error | Pass |
| 18 | Rollback | Book B1 + already booked A1 | Whole booking fails, B1 stays free | Pass |
| 19 | No seat selected | Press book | "Please select at least one seat." | Pass |
| 20 | More than 6 seats | Select 7 seats | Limit message | Pass |
| 21 | Invalid show ID | Show 9999 | "Show ... does not exist." | Pass |
| 22 | House full | Show with all seats booked | "No seats are available." | Pass |
| 23 | Two users book same seat at the same time | Two threads, seat D4 | Exactly one succeeds | Pass |
| 24 | Cancel booking | Booking of a future show | Status CANCELLED, seats free | Pass |
| 25 | Cancel twice | Cancel same booking again | "Already cancelled" | Pass |
| 26 | Cancel someone else's booking | Other user's booking ID | Refused | Pass |
| 27 | Late cancellation | Show starts in 1 hour | Refused (2-hour rule) | Pass |
| 28 | Statistics match database | Compare with SQL totals | Same numbers | Pass |
| 29 | Date filter | From/To dates | Correct count and revenue | Pass |
| 30 | Database down | Stop MySQL, start app | Error dialog, app exits cleanly | Pass |

(Test cases 1 to 29 were verified automatically against a MySQL-compatible database before delivery. Case 30 was verified by running with the database stopped: the connection class returns a clear error message, which the application shows in a dialog before exiting. Please also run through the screens once yourself and note your own results.)

## 9. Conclusion
The project delivers a complete working ticket booking system using only Core Java, Swing, JDBC and MySQL. It shows how a layered design keeps code organised, how JDBC transactions guarantee that seats are never double booked, how Streams turn raw rows into useful statistics, and how background threads keep the screen up to date without freezing. Custom exceptions give users clear messages instead of crashes.

## 10. Future Scope
- Store hashed passwords (BCrypt) instead of plain text.
- Online payment gateway integration and e-ticket PDF or QR code.
- Email or SMS confirmation and reminders.
- Different seat categories (Silver, Gold, Premium) with different prices.
- Seat-lock timer that holds a seat for a few minutes during payment.
- Movie posters, trailers and search or filter by genre and language.
- Web or mobile front end using the same database.
- Reports exported to Excel or PDF; connection pooling for better performance.

## 11. Concepts Used (viva explanations)

### JDBC
JDBC (Java Database Connectivity) is the Java API for talking to a database. `DriverManager.getConnection(url, user, password)` opens a connection; `PreparedStatement` runs SQL with `?` placeholders; `ResultSet` reads rows. In this project every DAO class uses try-with-resources so connections are always closed, and all SQL uses `PreparedStatement` to prevent SQL injection.

### Swing
Swing is Java's GUI toolkit. We use `JFrame` windows, `JPanel` containers, `JButton`, `JLabel`, `JTextField`, `JPasswordField`, `JComboBox`, `JTable` with `JScrollPane`, `JOptionPane` dialogs and `CardLayout` to switch screens. The seat map is a grid of `JButton` objects coloured by state.

### Java Streams
Streams process collections in a pipeline: source, intermediate operations (`filter`, `map`, `sorted`) and a terminal operation (`collect`, `count`, `sum`, `max`). `BookingAnalytics` uses them on booking data loaded from MySQL, for example `groupingBy` to count bookings per movie and `max` to find the most popular movie.

### Multithreading
`DashboardRefresher` and `ShowReminderMonitor` each use a `ScheduledExecutorService` with one background thread. The database work happens on that thread, and the result is passed to the screen with `SwingUtilities.invokeLater()`. Swing is not thread-safe, so components may only be changed on the Event Dispatch Thread. The statistics screen also uses a `SwingWorker` for the "Refresh Now" button.

### Transactions
A transaction groups several SQL statements into one all-or-nothing unit. `con.setAutoCommit(false)` starts it, `con.commit()` saves it and `con.rollback()` undoes it. Booking marks seats, creates the booking and links the seats in one transaction. The update `UPDATE seats SET is_booked = TRUE WHERE seat_id = ? AND is_booked = FALSE` returns 0 rows if someone else got the seat first, which triggers a rollback.

### Exception handling
Custom checked exceptions (for example `SeatAlreadyBookedException`) describe exactly what went wrong. DAOs convert `SQLException` into `DatabaseException`. The UI catches these exceptions and shows `JOptionPane` messages. No exception is silently ignored.
