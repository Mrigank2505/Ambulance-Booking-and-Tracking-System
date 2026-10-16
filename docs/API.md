# Ambulance Booking and Real-Time Tracking System - REST API Documentation

Base URL: `http://localhost:8080/api`  
Swagger / OpenAPI UI: `http://localhost:8080/swagger-ui.html`  
WebSocket Endpoint: `ws://localhost:8080/ws`

---

## 1. Authentication & Authorization

All secure endpoints require the HTTP header:
```http
Authorization: Bearer <jwt-token>
```

### POST `/api/auth/register`
Register a new account (default role: `USER`).
- **Request Body:**
```json
{
  "fullName": "Jane Doe",
  "email": "jane@example.com",
  "phone": "+1-800-555-0309",
  "password": "Password@123",
  "address": "15 Maple Avenue, Metro City",
  "role": "USER"
}
```
- **Response:** `201 Created`

### POST `/api/auth/login`
Authenticate user credentials and obtain JWT access token.
- **Request Body:**
```json
{
  "email": "john@example.com",
  "password": "Password@123"
}
```
- **Response:** `200 OK`
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "user": {
      "id": 6,
      "fullName": "John Doe",
      "email": "john@example.com",
      "role": "USER",
      "status": "ACTIVE"
    }
  }
}
```

### GET `/api/auth/me`
Retrieve profile of currently authenticated user.

---

## 2. Ambulances & Fleet Management

### GET `/api/ambulances`
List all ambulances with driver and status details.

### GET `/api/ambulances/available?type=ICU&lat=12.9716&lon=77.5946`
Fetch available fleet filtered by specification and sorted by Haversine distance.

### POST `/api/ambulances` *(ADMIN only)*
Register a new vehicle into the active fleet.
```json
{
  "ambulanceNumber": "AMB-ICU-108",
  "ambulanceType": "ICU",
  "capacity": 1,
  "equipmentDetails": "Dual Ventilator, Multi-parameter Monitor, Defibrillator",
  "currentLatitude": 12.9716,
  "currentLongitude": 77.5946
}
```

---

## 3. Hospitals & Trauma Centers

### GET `/api/hospitals`
List all medical centers.

### GET `/api/hospitals/nearby?lat=12.9716&lon=77.5946`
List nearby hospitals sorted by real-time distance from coordinates.

### POST `/api/hospitals` *(ADMIN only)*
Register a hospital facility.

---

## 4. Emergency Bookings & Dispatch

### POST `/api/bookings`
Create an emergency dispatch request. Intelligently selects the nearest suitable ambulance using Haversine formula and sends alerts.
- **Request Body:**
```json
{
  "patientName": "Michael Scott",
  "patientPhone": "+1-800-555-0388",
  "pickupAddress": "Scranton Business Park, 4th Floor",
  "pickupLatitude": 12.9716,
  "pickupLongitude": 77.5946,
  "hospitalId": 1,
  "emergencyType": "CRITICAL",
  "ambulanceType": "ICU",
  "notes": "Suspected cardiac arrest; AED requested on arrival."
}
```
- **Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": 3,
    "bookingCode": "AMB-BK-20261009-4821",
    "ambulanceNumber": "AMB-ICU-101",
    "driverName": "Mike Ross",
    "estimatedArrivalTime": "6 mins",
    "distanceKm": 1.8,
    "bookingStatus": "ASSIGNED"
  }
}
```

### GET `/api/bookings`
List current user's bookings.

### GET `/api/bookings/{id}`
Inspect booking by ID.

### PUT `/api/bookings/{id}/cancel`
Cancel an assigned or requested booking before patient pickup.

---

## 5. Driver Operations

### GET `/api/driver/me`
Retrieve profile of currently authenticated driver.

### PUT `/api/driver/availability`
Toggle driver status between `ONLINE` and `OFFLINE`.
```json
{
  "availabilityStatus": "ONLINE"
}
```

### GET `/api/driver/bookings/pending`
View pending emergency calls awaiting driver acceptance.

### PUT `/api/driver/bookings/{id}/accept`
Driver accepts emergency dispatch.

### PUT `/api/driver/bookings/{id}/arrived`
Driver marks arrival at patient pickup coordinates.

### PUT `/api/driver/bookings/{id}/picked-up`
Driver marks patient picked up and en-route to trauma center.

### PUT `/api/driver/bookings/{id}/completed`
Driver marks trip completed at hospital. Releases ambulance and driver back to available pool.

---

## 6. Real-Time Telemetry & Tracking

### GET `/api/tracking/{bookingId}`
Retrieve latest live GPS coordinates, ETA, velocity, and distance remaining.

### POST `/api/tracking/location`
Submit GPS telemetry ping from vehicle or mobile GPS.
```json
{
  "ambulanceId": 1,
  "bookingId": 3,
  "latitude": 12.9725,
  "longitude": 77.5955,
  "speed": 44.5
}
```

### POST `/api/tracking/{bookingId}/demo-step?stepRatio=0.2`
Simulate telemetry movement step along route for demonstration mode without hardware GPS.

---

## 7. WebSocket STOMP Messaging

Connect to WebSocket endpoint:
```
ws://localhost:8080/ws/websocket
```
Subscribe to topics:
- `/topic/ambulance/{ambulanceId}`: Receives live vehicle movement pings
- `/topic/booking/{bookingId}`: Receives trip status milestone changes and route telemetry

---

## 8. Admin Control Center

### GET `/api/admin/dashboard`
Returns high-level statistics and KPIs:
- `totalUsers`
- `totalDrivers`
- `totalAmbulances`
- `availableAmbulances`
- `activeBookings`
- `completedBookings`
- `emergencyBookings`
- `recentBookings`

### GET `/api/admin/users`
List all registered user accounts.

### PUT `/api/admin/users/{id}/status?status=SUSPENDED`
Toggle user account suspension.
