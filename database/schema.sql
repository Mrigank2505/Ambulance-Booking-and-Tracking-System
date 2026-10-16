-- ====================================================================
-- AMBULANCE BOOKING AND REAL-TIME TRACKING SYSTEM
-- Database Schema: ambulance_booking_db
-- Dialect: MySQL 8+
-- ====================================================================

CREATE DATABASE IF NOT EXISTS ambulance_booking_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ambulance_booking_db;

-- Drop tables in reverse foreign key order if re-executing
DROP TABLE IF EXISTS emergency_contacts;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS ambulance_locations;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS ambulances;
DROP TABLE IF EXISTS drivers;
DROP TABLE IF EXISTS hospitals;
DROP TABLE IF EXISTS users;

-- 1. USERS TABLE
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'USER', -- USER, DRIVER, ADMIN, HOSPITAL
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, SUSPENDED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email),
    INDEX idx_user_role (role)
) ENGINE=InnoDB;

-- 2. DRIVERS TABLE
CREATE TABLE drivers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    availability_status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE', -- ONLINE, OFFLINE, BUSY
    current_latitude DOUBLE,
    current_longitude DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_driver_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_driver_avail (availability_status)
) ENGINE=InnoDB;

-- 3. HOSPITALS TABLE
CREATE TABLE hospitals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    emergency_available BOOLEAN NOT NULL DEFAULT TRUE,
    icu_available BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_hospital_coords (latitude, longitude)
) ENGINE=InnoDB;

-- 4. AMBULANCES TABLE
CREATE TABLE ambulances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ambulance_number VARCHAR(50) NOT NULL UNIQUE,
    ambulance_type VARCHAR(20) NOT NULL, -- BASIC, ICU, ALS
    driver_id BIGINT NULL UNIQUE,
    capacity INT NOT NULL DEFAULT 1,
    equipment_details TEXT,
    availability_status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE, ASSIGNED, ON_TRIP, MAINTENANCE, OFFLINE
    current_latitude DOUBLE NOT NULL,
    current_longitude DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ambulance_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE SET NULL,
    INDEX idx_ambulance_status (availability_status),
    INDEX idx_ambulance_type (ambulance_type),
    INDEX idx_ambulance_coords (current_latitude, current_longitude)
) ENGINE=InnoDB;

-- 5. BOOKINGS TABLE
CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_code VARCHAR(30) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    ambulance_id BIGINT NULL,
    driver_id BIGINT NULL,
    hospital_id BIGINT NULL,
    patient_name VARCHAR(100) NOT NULL,
    patient_phone VARCHAR(20) NOT NULL,
    pickup_address VARCHAR(255) NOT NULL,
    pickup_latitude DOUBLE NOT NULL,
    pickup_longitude DOUBLE NOT NULL,
    destination_address VARCHAR(255),
    destination_latitude DOUBLE,
    destination_longitude DOUBLE,
    emergency_type VARCHAR(20) NOT NULL, -- CRITICAL, SERIOUS, NORMAL
    ambulance_type VARCHAR(20) NOT NULL, -- BASIC, ICU, ALS
    booking_status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED', 
    -- REQUESTED, SEARCHING, ASSIGNED, DRIVER_ACCEPTED, ON_THE_WAY, ARRIVED, PATIENT_PICKED_UP, REACHED_HOSPITAL, COMPLETED, CANCELLED, REJECTED
    notes TEXT,
    estimated_arrival_time VARCHAR(50),
    distance_km DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_booking_ambulance FOREIGN KEY (ambulance_id) REFERENCES ambulances(id) ON DELETE SET NULL,
    CONSTRAINT fk_booking_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE SET NULL,
    CONSTRAINT fk_booking_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id) ON DELETE SET NULL,
    INDEX idx_booking_code (booking_code),
    INDEX idx_booking_status (booking_status),
    INDEX idx_booking_user (user_id)
) ENGINE=InnoDB;

-- 6. AMBULANCE LOCATION HISTORY TABLE
CREATE TABLE ambulance_locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ambulance_id BIGINT NOT NULL,
    booking_id BIGINT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    speed DOUBLE NOT NULL DEFAULT 0.0,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_loc_ambulance FOREIGN KEY (ambulance_id) REFERENCES ambulances(id) ON DELETE CASCADE,
    CONSTRAINT fk_loc_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL,
    INDEX idx_ambulance_loc (ambulance_id),
    INDEX idx_loc_timestamp (timestamp)
) ENGINE=InnoDB;

-- 7. NOTIFICATIONS TABLE
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    booking_id BIGINT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'INFO', -- BOOKING, STATUS, ALERT, SYSTEM
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notif_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL,
    INDEX idx_notif_user (user_id),
    INDEX idx_notif_unread (user_id, is_read)
) ENGINE=InnoDB;

-- 8. EMERGENCY CONTACTS TABLE
CREATE TABLE emergency_contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    relationship VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_contact_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_contact_user (user_id)
) ENGINE=InnoDB;
