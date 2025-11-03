CREATE DATABASE reactivetravelbookingdb;

USE reactivetravelbookingdb;

SHOW TABLES;

-- Users table
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    user_type VARCHAR(50) DEFAULT 'USER',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Profiles table
CREATE TABLE profiles (
    profile_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    aadhar_number VARCHAR(20),
    city VARCHAR(50),
    phone_number VARCHAR(20),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Bookings table
CREATE TABLE bookings (
    booking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    destination VARCHAR(100),
    rate INT,
    booking_date DATE,
    number_of_people INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'PENDING',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);


INSERT INTO users (email, password_hash, user_type)
VALUES (
    'ashish@gmail.com',                      -- Email
    '$2a$12$chLNUBCgC4s.XrJAZ1l/GeSxi2fj50XixxmkPIrX672iJ31OSTOLa',  -- BCrypt hash of 'ashish1234'
    'USER'                                 -- UserType (Enum)
);

INSERT INTO profiles (user_id, first_name, last_name, aadhar_number, city, phone_number)
VALUES (1, 'Ashish', 'Shenoy', '123456789012', 'Mangalore', '9876543210');

INSERT INTO bookings (user_id, destination, rate, booking_date, number_of_people)
VALUES (1, 'Goa', 5000, '2025-11-01', 2);


INSERT INTO users (email, password_hash, user_type)
VALUES (
    'ashish1@gmail.com',                      -- Email
    '$2a$12$chLNUBCgC4s.XrJAZ1l/GeSxi2fj50XixxmkPIrX672iJ31OSTOLa',  -- BCrypt hash of 'ashish1234'
    'USER'                                 -- UserType (Enum)
);

INSERT INTO users (email, password_hash, user_type)
VALUES (
    'admin@gmail.com',                      -- Email
    '$2a$12$e0PK//bJCKG0fvWQ/xUMoOSVCPg81UcQn5dU3vczRDNKpGubfpdsm',  -- BCrypt hash of 'admin1234'
    'ADMIN'                                  -- UserType (Enum)
);

INSERT INTO users (email, password_hash, user_type)
VALUES (
    'shenoy@gmail.com',                      -- Email
    '$2a$12$/68PLVnu8QZVqIyQ72f/D.H04Ixetv1wp1YjU.6DswlZcButvo1AS',  -- BCrypt hash of 'shenoy1234'
    'USER'                                  -- UserType (Enum)
);

INSERT INTO profiles (user_id, first_name, last_name, aadhar_number, city, phone_number)
VALUES (4, 'Shenoy', 'BAshish', '123456789012', 'Mangalore', '9876543210');

INSERT INTO bookings (user_id, destination, rate, booking_date, number_of_people)
VALUES (4, 'Goa', 5000, '2025-11-01', 2);

INSERT INTO bookings (user_id, destination, rate, booking_date, number_of_people)
VALUES (1, 'Mysore', 15000, '2025-12-01', 5);

INSERT INTO profiles (first_name, last_name, aadhar_number, city, phone_number, user_id)
VALUES ('Admin', 'A', '123456789512', 'Mangalore', '9353431875', 3);

SELECT * FROM users;
SELECT * FROM profiles;
SELECT * FROM bookings;
SHOW profiles;