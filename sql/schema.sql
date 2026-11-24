-- =====================================================
-- Movie Ticket Booking Management System - Database
-- WARNING: this script deletes and recreates the database
-- =====================================================
DROP DATABASE IF EXISTS movie_booking_system;
CREATE DATABASE movie_booking_system;
USE movie_booking_system;

-- ---------- USERS ----------
CREATE TABLE users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,
    role        ENUM('ADMIN','CUSTOMER') NOT NULL DEFAULT 'CUSTOMER',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------- MOVIES ----------
CREATE TABLE movies (
    movie_id          INT AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(150) NOT NULL,
    genre             VARCHAR(50)  NOT NULL,
    language          VARCHAR(30)  NOT NULL,
    duration_minutes  INT NOT NULL CHECK (duration_minutes > 0),
    rating            DECIMAL(2,1) NOT NULL DEFAULT 0.0 CHECK (rating BETWEEN 0 AND 10),
    description       VARCHAR(500)
);

-- ---------- SHOWS (showtimes) ----------
CREATE TABLE shows (
    show_id       INT AUTO_INCREMENT PRIMARY KEY,
    movie_id      INT NOT NULL,
    show_date     DATE NOT NULL,
    show_time     TIME NOT NULL,
    screen_name   VARCHAR(30) NOT NULL,
    ticket_price  DECIMAL(8,2) NOT NULL CHECK (ticket_price > 0),
    total_seats   INT NOT NULL DEFAULT 40,
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE
);

-- ---------- SEATS (one row per seat per show) ----------
CREATE TABLE seats (
    seat_id      INT AUTO_INCREMENT PRIMARY KEY,
    show_id      INT NOT NULL,
    seat_number  VARCHAR(5) NOT NULL,
    is_booked    BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (show_id, seat_number),
    FOREIGN KEY (show_id) REFERENCES shows(show_id) ON DELETE CASCADE
);

-- ---------- BOOKINGS ----------
CREATE TABLE bookings (
    booking_id    INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT NOT NULL,
    show_id       INT NOT NULL,
    booking_date  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount  DECIMAL(10,2) NOT NULL,
    status        ENUM('CONFIRMED','CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (show_id) REFERENCES shows(show_id)
);

-- ---------- BOOKING_SEATS (which seats belong to which booking) ----------
CREATE TABLE booking_seats (
    booking_seat_id  INT AUTO_INCREMENT PRIMARY KEY,
    booking_id       INT NOT NULL,
    seat_id          INT NOT NULL,
    UNIQUE (booking_id, seat_id),
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id)    REFERENCES seats(seat_id)
);

-- =====================================================
-- SAMPLE DATA
-- =====================================================

-- Admin: admin@movie.com / admin123    Customer: rahul@gmail.com / rahul123
INSERT INTO users (full_name, email, password, role) VALUES
('System Admin', 'admin@movie.com', 'admin123', 'ADMIN'),
('Rahul Sharma', 'rahul@gmail.com', 'rahul123', 'CUSTOMER'),
('Priya Patil',  'priya@gmail.com', 'priya123', 'CUSTOMER');

INSERT INTO movies (title, genre, language, duration_minutes, rating, description) VALUES
('Inception',          'Sci-Fi',  'English', 148, 8.8, 'A thief who steals secrets through dreams gets one last impossible job.'),
('Interstellar',       'Sci-Fi',  'English', 169, 8.7, 'Explorers travel through a wormhole to save humanity.'),
('Avengers: Endgame',  'Action',  'English', 181, 8.4, 'The Avengers assemble once more to undo the damage of Thanos.'),
('3 Idiots',           'Comedy',  'Hindi',   170, 8.4, 'Three engineering students challenge the education system.'),
('Dangal',             'Drama',   'Hindi',   161, 8.3, 'A father trains his daughters to become wrestling champions.');

INSERT INTO shows (movie_id, show_date, show_time, screen_name, ticket_price, total_seats) VALUES
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00', 'Screen 1', 200.00, 40),
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '18:00:00', 'Screen 1', 250.00, 40),
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', 'Screen 2', 220.00, 40),
(3, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '19:30:00', 'Screen 2', 300.00, 40),
(4, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '16:00:00', 'Screen 3', 180.00, 40),
(5, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '21:00:00', 'Screen 3', 350.00, 40),
(2, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '11:00:00', 'Screen 1', 200.00, 40),
(3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '20:00:00', 'Screen 2', 280.00, 40);

-- Generate 40 seats (rows A-E, columns 1-8) for every show
INSERT INTO seats (show_id, seat_number)
SELECT s.show_id, CONCAT(r.row_letter, c.col_num)
FROM shows s
CROSS JOIN (SELECT 'A' AS row_letter UNION SELECT 'B' UNION SELECT 'C'
            UNION SELECT 'D' UNION SELECT 'E') r
CROSS JOIN (SELECT 1 AS col_num UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
            UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8) c
ORDER BY s.show_id, r.row_letter, c.col_num;

-- A few sample bookings (so analytics has data later)
INSERT INTO bookings (user_id, show_id, total_amount, status) VALUES
(2, 1, 400.00, 'CONFIRMED'),
(2, 3, 660.00, 'CONFIRMED'),
(3, 4, 300.00, 'CONFIRMED');

INSERT INTO booking_seats (booking_id, seat_id)
SELECT 1, seat_id FROM seats WHERE show_id = 1 AND seat_number IN ('A1','A2');
INSERT INTO booking_seats (booking_id, seat_id)
SELECT 2, seat_id FROM seats WHERE show_id = 3 AND seat_number IN ('B3','B4','B5');
INSERT INTO booking_seats (booking_id, seat_id)
SELECT 3, seat_id FROM seats WHERE show_id = 4 AND seat_number = 'C1';

UPDATE seats SET is_booked = TRUE WHERE seat_id IN (SELECT seat_id FROM booking_seats);
