# Database Design & Schema Specification

**Database Name:** `ambulance_booking_db`  
**RDBMS:** MySQL 8+ (InnoDB Engine)  
**Encoding:** UTF-8 Unicode (`utf8mb4`)

---

## 1. Entity-Relationship Overview

```
 [users] 1 ──── 1 [drivers]
    │                │
    │ 1            1 │
    │                │
    │              0..1
    ▼                ▼
[bookings] ─── [ambulances] 1 ─── 0..* [ambulance_locations]
    │
    ├───── 0..* [notifications]
    ▼
[hospitals]
```

---

## 2. Table Specifications

### 2.1 `users`
Stores authenticated credentials, platform roles, and basic user profiles.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique User Identifier |
| `full_name` | VARCHAR(100) | NOT NULL | User's legal full name |
| `email` | VARCHAR(100) | NOT NULL, UNIQUE, INDEX | Unique login email |
| `phone` | VARCHAR(20) | NOT NULL, UNIQUE | Mobile contact number |
| `password` | VARCHAR(255) | NOT NULL | BCrypt hashed password |
| `address` | VARCHAR(255) | NULL | Default residential address |
| `role` | VARCHAR(20) | NOT NULL, DEFAULT 'USER' | Role: `USER`, `DRIVER`, `ADMIN`, `HOSPITAL` |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | Account state: `ACTIVE`, `INACTIVE`, `SUSPENDED` |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Registration timestamp |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last profile update |

### 2.2 `drivers`
Stores professional driver licensing, shift availability, and telemetry anchor.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Driver record ID |
| `user_id` | BIGINT | NOT NULL, UNIQUE, FK -> `users(id)` | Associated user account |
| `license_number`| VARCHAR(50) | NOT NULL, UNIQUE | Commercial driving permit ID |
| `availability_status` | VARCHAR(20) | NOT NULL, DEFAULT 'OFFLINE' | Status: `ONLINE`, `OFFLINE`, `BUSY` |
| `current_latitude` | DOUBLE | NULL | Last reported GPS latitude |
| `current_longitude`| DOUBLE | NULL | Last reported GPS longitude |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Timestamp |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Timestamp |

### 2.3 `ambulances`
Active medical vehicle inventory, life-support specifications, and driver linkages.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Vehicle record ID |
| `ambulance_number` | VARCHAR(50) | NOT NULL, UNIQUE | Vehicle plate number (e.g. `AMB-ICU-101`) |
| `ambulance_type` | VARCHAR(20) | NOT NULL, INDEX | Type: `BASIC`, `ICU`, `ALS` |
| `driver_id` | BIGINT | NULL, UNIQUE, FK -> `drivers(id)` | Currently assigned driver |
| `capacity` | INT | NOT NULL, DEFAULT 1 | Patient stretcher capacity |
| `equipment_details`| TEXT | NULL | Summary of onboard medical devices |
| `availability_status`| VARCHAR(20)| NOT NULL, DEFAULT 'AVAILABLE' | `AVAILABLE`, `ASSIGNED`, `ON_TRIP`, `MAINTENANCE`, `OFFLINE` |
| `current_latitude` | DOUBLE | NOT NULL | Vehicle live GPS latitude |
| `current_longitude`| DOUBLE | NOT NULL | Vehicle live GPS longitude |

### 2.4 `hospitals`
Trauma facilities, emergency capacities, and destination geocodes.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Hospital facility ID |
| `name` | VARCHAR(150) | NOT NULL | Name of hospital |
| `address` | VARCHAR(255) | NOT NULL | Physical street address |
| `phone` | VARCHAR(20) | NOT NULL | 24/7 Casualty hotline |
| `latitude` | DOUBLE | NOT NULL | Geocode latitude |
| `longitude` | DOUBLE | NOT NULL | Geocode longitude |
| `emergency_available`| BOOLEAN | NOT NULL, DEFAULT TRUE | Has 24/7 emergency department |
| `icu_available` | BOOLEAN | NOT NULL, DEFAULT TRUE | Has open ICU beds |
| `description` | TEXT | NULL | Specialization overview |

### 2.5 `bookings`
Complete emergency request, vehicle allocation, and mission lifecycle record.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Booking ID |
| `booking_code` | VARCHAR(30) | NOT NULL, UNIQUE, INDEX | Reference format `AMB-BK-YYYYMMDD-XXXX` |
| `user_id` | BIGINT | NOT NULL, FK -> `users(id)` | Requesting patient/caller |
| `ambulance_id` | BIGINT | NULL, FK -> `ambulances(id)` | Allocated emergency vehicle |
| `driver_id` | BIGINT | NULL, FK -> `drivers(id)` | Driver operating the unit |
| `hospital_id` | BIGINT | NULL, FK -> `hospitals(id)` | Destination casualty center |
| `patient_name` | VARCHAR(100) | NOT NULL | Patient's name |
| `patient_phone` | VARCHAR(20) | NOT NULL | Callback phone number |
| `pickup_address` | VARCHAR(255) | NOT NULL | Staging / incident address |
| `pickup_latitude` | DOUBLE | NOT NULL | Incident GPS latitude |
| `pickup_longitude` | DOUBLE | NOT NULL | Incident GPS longitude |
| `destination_address`| VARCHAR(255)| NULL | Staging destination |
| `emergency_type` | VARCHAR(20) | NOT NULL | Severity: `CRITICAL`, `SERIOUS`, `NORMAL` |
| `ambulance_type` | VARCHAR(20) | NOT NULL | Requested type: `BASIC`, `ICU`, `ALS` |
| `booking_status` | VARCHAR(30) | NOT NULL, INDEX | `REQUESTED`, `SEARCHING`, `ASSIGNED`, `DRIVER_ACCEPTED`, `ON_THE_WAY`, `ARRIVED`, `PATIENT_PICKED_UP`, `REACHED_HOSPITAL`, `COMPLETED`, `CANCELLED` |
| `notes` | TEXT | NULL | Clinical condition notes |
| `estimated_arrival_time`| VARCHAR(50)| NULL | Dynamic ETA |
| `distance_km` | DOUBLE | NULL | Calculated Haversine route distance |

### 2.6 `ambulance_locations`
Breadcrumb telemetry history for GPS tracking. Indexed on `ambulance_id` and `timestamp`.

### 2.7 `notifications`
Milestone alerts, alert queues, and read states.

### 2.8 `emergency_contacts`
User-configured family or medical contacts notified during urgent dispatches.

---

## 3. Database Scripts

All schema definition DDL commands are available in:
- [schema.sql](file:///c:/Users/rohith/Desktop/ambulance/database/schema.sql)

All demo seed records and initial values are available in:
- [seed.sql](file:///c:/Users/rohith/Desktop/ambulance/database/seed.sql)
