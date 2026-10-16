-- ====================================================================
-- AMBULANCE BOOKING AND REAL-TIME TRACKING SYSTEM
-- Seed / Initial Demo Data
-- Database: ambulance_booking_db
-- ====================================================================

USE ambulance_booking_db;

-- 1. SEED USERS
-- Password for all seed users is: Password@123 (or Admin@123 for admin, Driver@123 for drivers, User@123 for patients)
-- Note: Spring Boot DataInitializer also verifies/upserts these demo users with freshly BCrypt hashed passwords.
-- BCrypt hash below corresponds to 'Password@123': $2a$10$7Rg7UjHkWzL1QYqG3gYj8.Z8oJqM8/nN3KzVdC3Y/oNlIe4IeO7vW

INSERT INTO users (id, full_name, email, phone, password, address, role, status) VALUES
(1, 'System Administrator', 'admin@ambulance.com', '+1-800-555-0100', '$2a$10$7Rg7UjHkWzL1QYqG3gYj8.Z8oJqM8/nN3KzVdC3Y/oNlIe4IeO7vW', 'Central Health Command Center, Metro City', 'ADMIN', 'ACTIVE'),
(2, 'Mike Ross', 'driver.mike@ambulance.com', '+1-800-555-0201', '$2a$10$7Rg7UjHkWzL1QYqG3gYj8.Z8oJqM8/nN3KzVdC3Y/oNlIe4IeO7vW', 'Station 1, North Avenue, Metro City', 'DRIVER', 'ACTIVE'),
(3, 'David Miller', 'driver.david@ambulance.com', '+1-800-555-0202', '$2a$10$7Rg7UjHkWzL1QYqG3gYj8.Z8oJqM8/nN3KzVdC3Y/oNlIe4IeO7vW', 'Station 3, West Boulevard, Metro City', 'DRIVER', 'ACTIVE'),
(4, 'Rajesh Patel', 'driver.raj@ambulance.com', '+1-800-555-0203', '$2a$10$7Rg7UjHkWzL1QYqG3gYj8.Z8oJqM8/nN3KzVdC3Y/oNlIe4IeO7vW', 'Station 2, South Highway, Metro City', 'DRIVER', 'ACTIVE'),
(5, 'Elena Rostova', 'driver.elena@ambulance.com', '+1-800-555-0204', '$2a$10$7Rg7UjHkWzL1QYqG3gYj8.Z8oJqM8/nN3KzVdC3Y/oNlIe4IeO7vW', 'Station 4, East Parkway, Metro City', 'DRIVER', 'ACTIVE'),
(6, 'John Doe', 'john@example.com', '+1-800-555-0301', '$2a$10$7Rg7UjHkWzL1QYqG3gYj8.Z8oJqM8/nN3KzVdC3Y/oNlIe4IeO7vW', '42 Elm Street, Metro City', 'USER', 'ACTIVE'),
(7, 'Sarah Connor', 'sarah@example.com', '+1-800-555-0302', '$2a$10$7Rg7UjHkWzL1QYqG3gYj8.Z8oJqM8/nN3KzVdC3Y/oNlIe4IeO7vW', '108 Oak Lane, Metro City', 'USER', 'ACTIVE'),
(8, 'Robert Langdon', 'robert@example.com', '+1-800-555-0303', '$2a$10$7Rg7UjHkWzL1QYqG3gYj8.Z8oJqM8/nN3KzVdC3Y/oNlIe4IeO7vW', '12 Pine Ridge, Metro City', 'USER', 'ACTIVE')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- 2. SEED DRIVERS
INSERT INTO drivers (id, user_id, license_number, availability_status, current_latitude, current_longitude) VALUES
(1, 2, 'DL-2024-8841', 'ONLINE', 12.9716, 77.5946),
(2, 3, 'DL-2024-9102', 'ONLINE', 12.9610, 77.6010),
(3, 4, 'DL-2024-3478', 'ONLINE', 12.9800, 77.5800),
(4, 5, 'DL-2024-5519', 'ONLINE', 12.9352, 77.6245)
ON DUPLICATE KEY UPDATE availability_status=VALUES(availability_status);

-- 3. SEED HOSPITALS
INSERT INTO hospitals (id, name, address, phone, latitude, longitude, emergency_available, icu_available, description) VALUES
(1, 'City Emergency & Trauma Hospital', '100 Medical Square, Central District', '+1-800-108-0001', 12.97244, 77.58064, TRUE, TRUE, 'Level 1 Trauma Center with 24/7 cardiac and pediatric emergency wings.'),
(2, 'Apollo Speciality Care Hospital', '25 Health Highway, South Park', '+1-800-108-0002', 12.96020, 77.59500, TRUE, TRUE, 'Multi-specialty center with advanced life support and stroke units.'),
(3, 'Fortis Emergency Medical Institute', '44 Ring Road, North Zone', '+1-800-108-0003', 12.98500, 77.60500, TRUE, TRUE, 'Equipped with rapid emergency response, ICU beds, and burn centers.'),
(4, 'Manipal Heart & Emergency Center', '88 Airport Road, East Corridor', '+1-800-108-0004', 12.95800, 77.64800, TRUE, FALSE, 'Specialized in cardiac emergencies, respiratory support, and ICU care.'),
(5, 'St. John Memorial Hospital', '12 Lake View Road, Koramangala', '+1-800-108-0005', 12.93440, 77.62190, TRUE, TRUE, 'Community hospital with 50 emergency beds, neonatology, and orthopedic surgery.')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 4. SEED AMBULANCES
INSERT INTO ambulances (id, ambulance_number, ambulance_type, driver_id, capacity, equipment_details, availability_status, current_latitude, current_longitude) VALUES
(1, 'AMB-ICU-101', 'ICU', 1, 1, 'Ventilator, Defibrillator, Multipara Monitor, Infusion Pumps, Portable Oxygen', 'AVAILABLE', 12.9716, 77.5946),
(2, 'AMB-BSC-102', 'BASIC', 2, 2, 'Stretcher, First Aid Kit, Oxygen Cylinder, Basic BP/Pulse Monitors', 'AVAILABLE', 12.9610, 77.6010),
(3, 'AMB-ALS-103', 'ALS', 3, 1, 'Cardiac Monitor, Defibrillator, Advanced Airway Gear, IV Kit, Suction Unit', 'AVAILABLE', 12.9800, 77.5800),
(4, 'AMB-ICU-104', 'ICU', 4, 1, 'Full Critical Care Support, ECMO compatible, Dual Ventilator, Blood Gas Analyzer', 'AVAILABLE', 12.9352, 77.6245),
(5, 'AMB-BSC-105', 'BASIC', NULL, 1, 'First Aid, Stretcher, Oxygen delivery', 'MAINTENANCE', 12.9500, 77.6100)
ON DUPLICATE KEY UPDATE availability_status=VALUES(availability_status);

-- 5. SEED SAMPLE BOOKINGS
INSERT INTO bookings (id, booking_code, user_id, ambulance_id, driver_id, hospital_id, patient_name, patient_phone, pickup_address, pickup_latitude, pickup_longitude, destination_address, destination_latitude, destination_longitude, emergency_type, ambulance_type, booking_status, notes, estimated_arrival_time, distance_km) VALUES
(1, 'AMB-BK-20261001-01', 6, 2, 2, 1, 'Mark Doe', '+1-800-555-0301', '42 Elm Street, Metro City', 12.9650, 77.5980, 'City Emergency & Trauma Hospital', 12.97244, 77.58064, 'NORMAL', 'BASIC', 'COMPLETED', 'Patient stabilized and admitted smoothly.', 'Arrived in 8 mins', 2.3),
(2, 'AMB-BK-20261002-02', 7, 3, 3, 2, 'Sarah Connor', '+1-800-555-0302', '108 Oak Lane, Metro City', 12.9780, 77.5840, 'Apollo Speciality Care Hospital', 12.96020, 77.59500, 'SERIOUS', 'ALS', 'COMPLETED', 'Accidental fracture, transported safely.', 'Arrived in 6 mins', 3.1)
ON DUPLICATE KEY UPDATE booking_status=VALUES(booking_status);

-- 6. SEED NOTIFICATIONS
INSERT INTO notifications (id, user_id, booking_id, title, message, type, is_read) VALUES
(1, 6, 1, 'Booking Completed', 'Your ambulance trip AMB-BK-20261001-01 has been marked completed. Thank you for using our service.', 'STATUS', TRUE),
(2, 6, NULL, 'System Welcome', 'Welcome to Ambulance Direct. In case of emergency, call 108 or hit Quick Book.', 'INFO', TRUE),
(3, 7, 2, 'Booking Completed', 'Trip AMB-BK-20261002-02 delivered patient to Apollo Speciality Care Hospital.', 'STATUS', TRUE),
(4, 1, NULL, 'System Ready', 'All 4 response units are online and standing by.', 'SYSTEM', FALSE)
ON DUPLICATE KEY UPDATE is_read=VALUES(is_read);

-- 7. SEED EMERGENCY CONTACTS
INSERT INTO emergency_contacts (id, user_id, name, phone, relationship) VALUES
(1, 6, 'Jane Doe', '+1-800-555-0399', 'Spouse'),
(2, 6, 'Dr. Samuel Vance', '+1-800-555-0398', 'Family Physician'),
(3, 7, 'John Connor', '+1-800-555-0397', 'Son')
ON DUPLICATE KEY UPDATE name=VALUES(name);
