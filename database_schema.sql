-- ====================================================================
-- SMART HOME IOT DASHBOARD - DATABASE SCHEMA
-- ====================================================================
-- Database: smarthomeapp
-- DBMS: MySQL 8.0
-- Date: December 12, 2025
-- ====================================================================

-- Create database
CREATE DATABASE IF NOT EXISTS smarthomeapp;
USE smarthomeapp;

-- ====================================================================
-- TABLE 1: USERS
-- ====================================================================
-- Stores user information and preferences for AI automation
-- ====================================================================

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    password VARCHAR(255),
    preferred_temperature DOUBLE COMMENT 'User preferred temperature for AI suggestions (Celsius)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample data
INSERT INTO users (username, email, password, preferred_temperature) VALUES
('carla', 'carla@smarthome.com', 'password123', 22.0),
('admin', 'admin@smarthome.com', 'admin123', 21.5);

-- ====================================================================
-- TABLE 2: ROOMS
-- ====================================================================
-- Stores room information linked to users
-- Relationship: Many-to-One with Users (One User has many Rooms)
-- ====================================================================

CREATE TABLE rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT 'Room name (e.g., Living Room, Bedroom)',
    floor INTEGER NOT NULL COMMENT 'Floor level (0 = ground floor, 1 = first floor, etc.)',
    user_id BIGINT NOT NULL COMMENT 'Foreign key to users table',

    CONSTRAINT fk_room_user FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample data
INSERT INTO rooms (name, floor, user_id) VALUES
('Living Room', 0, 1),
('Bedroom', 1, 1),
('Kitchen', 0, 1),
('Office', 1, 2);

-- ====================================================================
-- TABLE 3: DEVICES
-- ====================================================================
-- Stores IoT device information
-- Relationship: Many-to-One with Rooms (One Room has many Devices)
-- ====================================================================

CREATE TABLE devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT 'Device name (e.g., Main Light, Thermostat)',
    type VARCHAR(50) NOT NULL COMMENT 'Device type: LIGHT, THERMOSTAT, or LOCK',
    status BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Device status: true = ON, false = OFF',
    value DOUBLE COMMENT 'Current reading/value (e.g., temperature for thermostats, brightness for lights)',
    room_id BIGINT NOT NULL COMMENT 'Foreign key to rooms table',

    CONSTRAINT fk_device_room FOREIGN KEY (room_id)
        REFERENCES rooms(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_device_type CHECK (type IN ('LIGHT', 'THERMOSTAT', 'LOCK'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample data
INSERT INTO devices (name, type, status, value, room_id) VALUES
('Main Light', 'LIGHT', TRUE, 100.0, 1),
('Reading Lamp', 'LIGHT', FALSE, 0.0, 2),
('Living Room Thermostat', 'THERMOSTAT', TRUE, 22.0, 1),
('Bedroom Thermostat', 'THERMOSTAT', TRUE, 20.0, 2),
('Front Door Lock', 'LOCK', TRUE, 1.0, 1),
('Kitchen Light', 'LIGHT', TRUE, 75.0, 3);

-- ====================================================================
-- RELATIONSHIPS SUMMARY
-- ====================================================================
--
-- 1. User → Room (One-to-Many)
--    - One user can own multiple rooms
--    - rooms.user_id references users.id
--    - CASCADE DELETE: Deleting a user removes all their rooms
--
-- 2. Room → Device (One-to-Many)
--    - One room can contain multiple devices
--    - devices.room_id references rooms.id
--    - CASCADE DELETE: Deleting a room removes all its devices
--
-- 3. User → Device (Indirect through Room)
--    - Users manage devices through room ownership
--
-- ====================================================================

-- ====================================================================
-- INDEXES FOR PERFORMANCE
-- ====================================================================

-- Index for filtering devices by room
CREATE INDEX idx_device_room ON devices(room_id);

-- Index for filtering devices by status (active/inactive)
CREATE INDEX idx_device_status ON devices(status);

-- Index for filtering devices by type
CREATE INDEX idx_device_type ON devices(type);

-- Index for user lookup by username (login)
CREATE INDEX idx_user_username ON users(username);

-- Index for rooms by user
CREATE INDEX idx_room_user ON rooms(user_id);

-- ====================================================================
-- CUSTOM QUERIES (Used by Backend API)
-- ====================================================================

-- Query 1: Get all devices in a specific room
-- Endpoint: GET /api/devices/room/{roomId}
-- SELECT * FROM devices WHERE room_id = ?;

-- Query 2: Get all active devices
-- Endpoint: GET /api/devices/active?status=true
-- SELECT * FROM devices WHERE status = true;

-- Query 3: Get all devices by type
-- SELECT * FROM devices WHERE type = 'THERMOSTAT';

-- Query 4: Get user's preferred temperature for AI
-- SELECT preferred_temperature FROM users WHERE id = ?;

-- Query 5: Count devices per room
-- SELECT r.name, COUNT(d.id) as device_count
-- FROM rooms r
-- LEFT JOIN devices d ON r.id = d.room_id
-- GROUP BY r.id, r.name;

-- Query 6: Get all thermostats below preferred temperature
-- SELECT d.*
-- FROM devices d
-- JOIN rooms r ON d.room_id = r.id
-- JOIN users u ON r.user_id = u.id
-- WHERE d.type = 'THERMOSTAT'
--   AND d.value < u.preferred_temperature;

-- ====================================================================
-- BUSINESS RULES & CONSTRAINTS
-- ====================================================================

-- 1. A room must have an owner (user_id NOT NULL)
-- 2. A device must belong to a room (room_id NOT NULL)
-- 3. Device type must be one of: LIGHT, THERMOSTAT, LOCK
-- 4. Device status is boolean (ON/OFF)
-- 5. Temperature values are in Celsius
-- 6. Cascade deletion: User → Rooms → Devices

-- ====================================================================
-- VERIFICATION QUERIES
-- ====================================================================

-- Count tables
SELECT
    (SELECT COUNT(*) FROM users) as total_users,
    (SELECT COUNT(*) FROM rooms) as total_rooms,
    (SELECT COUNT(*) FROM devices) as total_devices;

-- Verify relationships
SELECT
    u.username,
    COUNT(DISTINCT r.id) as room_count,
    COUNT(d.id) as device_count
FROM users u
LEFT JOIN rooms r ON u.id = r.user_id
LEFT JOIN devices d ON r.id = d.room_id
GROUP BY u.id, u.username;

-- ====================================================================
-- END OF SCHEMA
-- ====================================================================

