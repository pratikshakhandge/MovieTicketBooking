# 30 Viva Questions with Simple Answers

**1. What is your project about?**
A desktop Movie Ticket Booking system. Customers book seats; the admin manages movies and sees statistics. It uses Java, Swing, JDBC and MySQL.

**2. Why did you use Swing instead of JavaFX?**
Swing is part of the standard JDK, needs no extra setup and is simple for beginners. The college requirement was Swing.

**3. What is JDBC?**
Java Database Connectivity, the standard Java API to connect to and run SQL on a database.

**4. What is the JDBC connection URL of your project?**
`jdbc:mysql://localhost:3307/movie_booking_system?...`. It names the driver, host, port and database.

**5. Why do you need the MySQL Connector/J jar?**
It is the JDBC driver. It translates JDBC calls into the MySQL network protocol. Without it Java cannot talk to MySQL.

**6. What is a PreparedStatement and why use it?**
A precompiled SQL statement with `?` placeholders. It prevents SQL injection and is faster for repeated queries.

**7. What is SQL injection?**
An attack where the user types SQL code into an input field. PreparedStatement treats the input only as data, so the attack fails.

**8. What is try-with-resources?**
A try block that automatically closes Connection, Statement and ResultSet when it ends, even if an exception occurs.

**9. Explain the layered architecture of your project.**
UI calls Service, Service calls DAO, DAO talks to MySQL. The UI never has SQL; the DAO never has business rules.

**10. What is a DAO?**
Data Access Object: a class whose only job is database access (SQL) for one entity, like `MovieDAO`.

**11. Why do you have a service layer?**
It holds business rules and validation, such as the maximum 6 seats and the 2-hour cancellation rule, so the UI and DAO stay simple.

**12. What is a model class?**
A plain Java class that holds the data of one table, for example `Movie` with fields, getters and setters.

**13. What is a transaction?**
A group of SQL statements that either all succeed (commit) or all fail (rollback).

**14. Where do you use transactions in your project?**
While booking seats, cancelling a booking and adding a show with its 40 seats.

**15. How do you prevent double booking?**
Each seat is updated with `WHERE is_booked = FALSE`. If zero rows change, someone else booked it, so we throw `SeatAlreadyBookedException` and roll back. A UNIQUE constraint on (show_id, seat_number) also protects the data.

**16. What happens if booking fails in the middle?**
`rollback()` undoes every change of that booking, so no seat stays marked as booked and no half booking is saved.

**17. What is `setAutoCommit(false)`?**
It stops JDBC from saving each statement immediately, so we can commit or roll back many statements together.

**18. Explain the database tables.**
`users`, `movies`, `shows`, `seats`, `bookings` and `booking_seats`, linked with primary and foreign keys.

**19. Why do you have a `booking_seats` table?**
One booking can contain many seats, so a separate table links a booking to its seats (a many-to-many relationship).

**20. What are primary key and foreign key?**
A primary key uniquely identifies a row. A foreign key points to the primary key of another table and keeps data consistent.

**21. What is `ON DELETE CASCADE`?**
When a parent row is deleted, its child rows are deleted automatically. For example, deleting a show deletes its seats.

**22. What are Java Streams?**
A way to process collections with a pipeline: source, then operations like `filter` and `map`, then a final operation like `collect` or `count`.

**23. Which Stream methods did you use?**
`filter`, `map`, `mapToDouble`, `mapToInt`, `sorted`, `collect`, `count`, `sum`, `groupingBy` and `max`.

**24. How do you find the most popular movie?**
`groupingBy(movieTitle, counting())` gives bookings per movie; `max()` on those counts gives the winner.

**25. Why does analytics use data from the database?**
DAO classes fetch real bookings from MySQL; `BookingAnalytics` then calculates results on that list with Streams.

**26. What is multithreading and where is it used?**
Running tasks at the same time on different threads. `DashboardRefresher` refreshes admin statistics every 10 seconds and `ShowReminderMonitor` checks for upcoming shows every 30 seconds.

**27. What is ScheduledExecutorService?**
A thread pool that runs a task repeatedly after a delay. We use `scheduleWithFixedDelay`.

**28. What is the Event Dispatch Thread? Why `SwingUtilities.invokeLater()`?**
The EDT is the one thread that draws and updates Swing components. Swing is not thread-safe, so background threads must pass screen updates to the EDT with `invokeLater`.

**29. How do you avoid freezing the Swing GUI?**
Slow work (database queries) runs on a background thread or in a `SwingWorker`; only the final screen update runs on the EDT.

**30. How do you handle exceptions in your project?**
Custom checked exceptions describe each problem (invalid login, duplicate user, seat already booked, no seats, invalid show, cancellation error, database error). DAOs wrap `SQLException` in `DatabaseException`. The UI catches them and shows `JOptionPane` messages. Nothing is silently ignored.

---
## Bonus questions
**Difference between checked and unchecked exceptions?** Checked exceptions must be handled or declared (like `SQLException`); unchecked ones (like `NullPointerException`) need not be.
**What is CardLayout?** A layout that stacks panels and shows one at a time; used for switching dashboard screens.
**Difference between `Statement` and `PreparedStatement`?** `Statement` builds SQL by joining text (unsafe); `PreparedStatement` uses placeholders (safe and faster).
**Why are passwords not hashed?** To keep the project simple. In real systems we would store BCrypt hashes. This is listed in future scope.
**What is a record in `analytics`?** A short way to write an immutable data class (`MovieStat`, `DashboardStats`).
