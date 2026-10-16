# Quick Setup & Deployment Guide

This document explains how to set up, configure, and launch the **Ambulance Booking and Real-Time Tracking System** locally.

---

## 1. Prerequisites

Ensure the following tools are installed on your workstation:
- **Java Development Kit (JDK):** Version 17 or higher (JDK 17, 21, or 26)
- **Apache Maven:** 3.8+ (Provided in `tools/apache-maven-3.9.6`)
- **MySQL Server:** 8.0 or newer (Optional: The application automatically falls back to an in-memory MySQL-compatible H2 database if MySQL is not currently running)
- **Web Browser:** Chrome, Edge, Firefox, or Safari

---

## 2. Database Setup (MySQL)

1. Open your MySQL client (MySQL Workbench, Command Line, or phpMyAdmin) and create the database:
   ```sql
   CREATE DATABASE IF NOT EXISTS ambulance_booking_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Run the provided schema script:
   ```bash
   mysql -u root -p ambulance_booking_db < database/schema.sql
   ```
3. Run the provided seed script to populate demo users, ambulances, and hospitals:
   ```bash
   mysql -u root -p ambulance_booking_db < database/seed.sql
   ```

*(Note: Spring Boot's built-in `DataInitializer` will also automatically verify and seed all records upon startup if the database is fresh).*

---

## 3. Backend Setup & Startup

1. Open a terminal in the `backend/` directory:
   ```powershell
   cd c:\Users\rohith\Desktop\ambulance\backend
   ```
2. Verify configuration in `src/main/resources/application.yml` or set environment variables if needed:
   - `DB_URL`: Defaults to `jdbc:mysql://localhost:3306/ambulance_booking_db`
   - `DB_USERNAME`: `root`
   - `DB_PASSWORD`: `Root@123` (or your local MySQL root password)
   - `JWT_SECRET`: Base64 HMAC-SHA256 key
3. Build and run the Spring Boot application:
   ```powershell
   ..\tools\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
   ```
4. Verify backend health:
   - API Root: `http://localhost:8080/api`
   - Swagger Documentation: `http://localhost:8080/swagger-ui.html`
   - WebSocket Endpoint: `ws://localhost:8080/ws`

---

## 4. Frontend Setup & Startup

The frontend is built purely with **HTML5, CSS3, and Vanilla JavaScript** with **no Node.js build step or npm install required**.

You can run it using any static HTTP server. For example:

### Option A: Using Python (built into most machines)
```powershell
cd c:\Users\rohith\Desktop\ambulance\frontend
python -m http.server 3000
```
Open your browser at: `http://localhost:3000/index.html`

### Option B: Using VS Code Live Server
Right-click on `frontend/index.html` and choose **"Open with Live Server"**.

### Option C: Opening directly in browser
Double-click `frontend/index.html` in Windows File Explorer. (CORS is pre-configured to allow local file & origin access).

---

## 5. Demo Credentials

The system comes pre-seeded with valid demonstration accounts:

| Role | Email | Password | Access Level |
|---|---|---|---|
| **Admin** | `admin@ambulance.com` | `Password@123` | Full Fleet, User, Hospital & Booking Management |
| **Driver** | `driver.mike@ambulance.com` | `Password@123` | Cockpit, Accept/Reject Dispatches, Status Transitions |
| **Driver 2** | `driver.david@ambulance.com`| `Password@123` | Standby Ambulance Unit AMB-BSC-102 |
| **User (Patient)** | `john@example.com` | `Password@123` | Book Ambulance, Real-Time Tracking, History |
| **User 2** | `sarah@example.com` | `Password@123` | Historical bookings & Emergency Contacts |

*Note: The login page includes 1-click **Quick Demo Login** buttons for instant testing.*
