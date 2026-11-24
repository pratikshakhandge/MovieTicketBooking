# Movie Ticket Booking Management System

College mini-project built with **Core Java, Swing, JDBC and MySQL** only
(no JavaFX, Spring Boot, Hibernate or web technologies).

**Status: COMPLETE (all 10 phases).** Login and registration, admin dashboard, customer dashboard,
seat booking with transactions, booking history and cancellation, Streams analytics,
background threads, report and viva questions.

---

## 1. Technology Stack

| Item | Value |
|---|---|
| Language | Java 26 (code also compiles on Java 17 or newer) |
| GUI | Java Swing (JFrame, JPanel, JTable, CardLayout ...) |
| Database | MySQL, server port **3307**, database `movie_booking_system` |
| Connectivity | JDBC (MySQL Connector/J jar) |
| Tools | VS Code, MySQL Workbench |

---

## 2. QUICK START (5 steps)

1. **Create the database:** open `sql/schema.sql` in MySQL Workbench (connection: localhost, port 3307, user root) and press the lightning bolt.
2. **Add the JDBC driver:** download *Connector/J* (Platform Independent ZIP) from https://dev.mysql.com/downloads/connector/j/ and copy `mysql-connector-j-x.x.x.jar` into the `lib/` folder.
3. **Set your password:** open `src/database/DBConnection.java` and change `PASSWORD = "your_mysql_password"`.
4. **Compile** (in the VS Code terminal, project root folder):
   - Windows: `.\compile.bat`
   - Mac/Linux: `./compile.sh`
5. **Run the application:**
   - Windows: `.\run.bat`
   - Mac/Linux: `./run.sh`

**Login accounts (from the SQL script)**

| Role | Email | Password |
|---|---|---|
| Admin | admin@movie.com | admin123 |
| Customer | rahul@gmail.com | rahul123 |
| Customer | priya@gmail.com | priya123 |

You can also register a new customer on the login screen.

---

## 3. Detailed Execution Steps (Windows)

### Step 1: Check Java
```
java -version
javac -version
```
Both must show version 26 (or at least 17). Install "Extension Pack for Java" in VS Code.

### Step 2: Database
1. Make sure MySQL Server is running on port **3307**.
2. In Workbench: File -> Open SQL Script -> `sql/schema.sql` -> Execute (lightning bolt).
3. Check:
```sql
USE movie_booking_system;
SELECT COUNT(*) FROM movies;   -- 5
SELECT COUNT(*) FROM shows;    -- 8
SELECT COUNT(*) FROM seats;    -- 320
```
> Running the script again DELETES and recreates the database (it starts with DROP DATABASE).
> Sample shows are dated relative to the day you run the script (tomorrow, +2 days, +3 days).
> If you run the project on a later day, run the script again, or add new shows from the admin screen.

### Step 3: JDBC driver
Copy `mysql-connector-j-x.x.x.jar` into `lib/`.

### Step 4: Password
Edit `PASSWORD` in `src/database/DBConnection.java`. Settings used:
host `localhost`, port `3307`, database `movie_booking_system`, user `root`.

### Step 5: Open in VS Code
File -> Open Folder -> `MovieTicketBooking`. The file `.vscode/settings.json` is already configured.

### Step 6: Compile
```powershell
.\compile.bat
```
or manually:
```powershell
javac -cp "lib/*" -d bin (Get-ChildItem -Recurse -Filter *.java src).FullName
```
No message = success. (`-cp "lib/*"` = look for libraries in lib, `-d bin` = write class files to bin.)

### Step 7: Test only the database connection (optional)
```powershell
java -cp "bin;lib/*" database.TestConnection
```

### Step 8: Run the application
```powershell
.\run.bat
```
or manually:
```powershell
java -cp "bin;lib/*" main.Main
```
On Mac/Linux use `:` instead of `;` in the classpath: `java -cp "bin:lib/*" main.Main`.
Every time you change a `.java` file, compile again before running.

---

## 4. Project Structure

```
MovieTicketBooking/
├── .vscode/settings.json
├── lib/                          mysql-connector-j-x.x.x.jar goes here
├── sql/schema.sql                database script (tables + sample data)
├── docs/
│   ├── PROJECT_REPORT.md         abstract, objectives, architecture, test cases, concepts ...
│   ├── VIVA_QUESTIONS.md         30 viva questions with answers
│   └── screenshots/              screens of the application
├── compile.bat / compile.sh      compile helper
├── run.bat / run.sh              start the application
├── run-test.bat / run-test.sh    database connection test
└── src/
    ├── main/Main.java
    ├── model/       User, Movie, Show, Seat, Booking
    ├── database/    DBConnection, TestConnection
    ├── dao/         UserDAO, MovieDAO, ShowDAO, SeatDAO, BookingDAO
    ├── service/     Validator, AuthService, MovieService, ShowService, BookingService
    ├── analytics/   BookingAnalytics, AnalyticsService, DashboardStats, MovieStat
    ├── threads/     DashboardRefresher, ShowReminderMonitor
    ├── exception/   DatabaseException + 7 custom exceptions
    └── ui/          Theme, LoginFrame, AdminDashboard (+4 panels), CustomerDashboard (+2 panels),
                     MovieDetailsDialog, SeatSelectionDialog, BookingConfirmationDialog
```

Layered flow: `UI -> Service -> DAO -> MySQL`

---

## 5. Features (how each requirement is covered)

| Requirement | Where |
|---|---|
| Packages and modular structure | 9 packages (see structure) |
| JDBC + MySQL persistence | `dao` package, `DBConnection`; every query uses `PreparedStatement` |
| Transactions with rollback | `BookingDAO.createBooking`, `BookingDAO.cancelBooking`, `ShowDAO.insertShow` |
| Prevent double booking | `UPDATE seats ... WHERE is_booked = FALSE` + rollback + UNIQUE constraints |
| Java Streams analytics | `BookingAnalytics`: filter, map, sorted, collect, count, sum, groupingBy, max |
| Multithreading | `DashboardRefresher` (admin stats every 10 s) and `ShowReminderMonitor` (customer reminders every 30 s), both with `ScheduledExecutorService`; `SwingWorker` for "Refresh Now" |
| Exception handling | 8 custom exceptions, messages shown with `JOptionPane` |
| Role-based access | Admin and customer dashboards after login |

### Admin can
Add, update, delete and view movies; add showtimes (40 seats created automatically); view available/booked seats of any show; view all bookings; see total bookings, total revenue, tickets sold, average ticket price, popular movies, seat occupancy; filter bookings by date.

### Customer can
Register and login; browse movie cards; see details and showtimes; pick seats on a colour-coded seat map; book (max 6 seats); see the confirmation with Booking ID; view booking history and tickets; cancel (until 2 hours before the show); get reminders for shows within 48 hours.

### Business rules
- Maximum **6 seats** per booking.
- Cancellation allowed only until **2 hours before** the show, only for your own CONFIRMED bookings.
- Shows in the past cannot be booked or created.
- A movie that has bookings cannot be deleted.

---

## 6. Common Errors and Fixes

| Error | Fix |
|---|---|
| `Access denied for user 'root'` | Wrong password in `DBConnection.java`. Fix it and compile again. |
| `Communications link failure` | MySQL not running, or port is not 3307. |
| `Unknown database 'movie_booking_system'` | Run `sql/schema.sql` first. |
| `No suitable driver found` | The jar is missing from `lib/`. |
| `Could not find or load main class main.Main` | Compile first and run from the project root folder. |
| `javac is not recognized` | Install the JDK and add its `bin` folder to PATH; restart VS Code. |
| `Public Key Retrieval is not allowed` | URL must contain `allowPublicKeyRetrieval=true` (already included). |
| Movie list shows no showtimes | Sample shows are dated a few days after the day you ran the script. Run `schema.sql` again or add shows in the admin panel. |
| Windows classpath error | Windows uses `;` (`bin;lib/*`), Mac/Linux use `:` (`bin:lib/*`). |
| "This movie cannot be deleted" | The movie already has bookings (intended behaviour). |

---

## 7. Documentation for submission
- `docs/PROJECT_REPORT.md`: abstract, problem statement, objectives, requirements, database schema, architecture, modules, testing cases, conclusion, future scope, explanation of JDBC, Swing, Streams, multithreading, transactions and exception handling.
- `docs/VIVA_QUESTIONS.md`: 30 viva questions with simple answers (plus bonus questions).
- `docs/screenshots/`: images of the screens for your report.
