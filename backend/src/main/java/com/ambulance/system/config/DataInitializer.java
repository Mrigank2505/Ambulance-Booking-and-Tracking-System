package com.ambulance.system.config;

import com.ambulance.system.entity.*;
import com.ambulance.system.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final HospitalRepository hospitalRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final BookingRepository bookingRepository;
    private final NotificationRepository notificationRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           DriverRepository driverRepository,
                           HospitalRepository hospitalRepository,
                           AmbulanceRepository ambulanceRepository,
                           BookingRepository bookingRepository,
                           NotificationRepository notificationRepository,
                           EmergencyContactRepository emergencyContactRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.hospitalRepository = hospitalRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.bookingRepository = bookingRepository;
        this.notificationRepository = notificationRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        ensureRegionalHospitals();

        if (userRepository.count() > 0) {
            log.info("Database already initialized with {} users. Skipping seed initialization.", userRepository.count());
            return;
        }

        log.info("Initializing demo seed data into database...");

        String defaultPass = passwordEncoder.encode("Password@123");

        // 1. Seed Users
        User admin = userRepository.save(new User(null, "System Administrator", "admin@ambulance.com", "+1-800-555-0100", defaultPass, "Central Health Command Center, Metro City", Role.ADMIN, UserStatus.ACTIVE));
        User driverUser1 = userRepository.save(new User(null, "Mike Ross", "driver.mike@ambulance.com", "+1-800-555-0201", defaultPass, "Station 1, North Avenue, Metro City", Role.DRIVER, UserStatus.ACTIVE));
        User driverUser2 = userRepository.save(new User(null, "David Miller", "driver.david@ambulance.com", "+1-800-555-0202", defaultPass, "Station 3, West Boulevard, Metro City", Role.DRIVER, UserStatus.ACTIVE));
        User driverUser3 = userRepository.save(new User(null, "Rajesh Patel", "driver.raj@ambulance.com", "+1-800-555-0203", defaultPass, "Station 2, South Highway, Metro City", Role.DRIVER, UserStatus.ACTIVE));
        User driverUser4 = userRepository.save(new User(null, "Elena Rostova", "driver.elena@ambulance.com", "+1-800-555-0204", defaultPass, "Station 4, East Parkway, Metro City", Role.DRIVER, UserStatus.ACTIVE));

        User patient1 = userRepository.save(new User(null, "John Doe", "john@example.com", "+1-800-555-0301", defaultPass, "42 Elm Street, Metro City", Role.USER, UserStatus.ACTIVE));
        User patient2 = userRepository.save(new User(null, "Sarah Connor", "sarah@example.com", "+1-800-555-0302", defaultPass, "108 Oak Lane, Metro City", Role.USER, UserStatus.ACTIVE));
        User patient3 = userRepository.save(new User(null, "Robert Langdon", "robert@example.com", "+1-800-555-0303", defaultPass, "12 Pine Ridge, Metro City", Role.USER, UserStatus.ACTIVE));

        // 2. Seed Drivers
        Driver driver1 = driverRepository.save(new Driver(null, driverUser1, "DL-2024-8841", DriverAvailability.ONLINE, 12.9716, 77.5946));
        Driver driver2 = driverRepository.save(new Driver(null, driverUser2, "DL-2024-9102", DriverAvailability.ONLINE, 12.9610, 77.6010));
        Driver driver3 = driverRepository.save(new Driver(null, driverUser3, "DL-2024-3478", DriverAvailability.ONLINE, 12.9800, 77.5800));
        Driver driver4 = driverRepository.save(new Driver(null, driverUser4, "DL-2024-5519", DriverAvailability.ONLINE, 12.9352, 77.6245));

        // 3. Seed Hospitals
        Hospital hosp1 = hospitalRepository.save(new Hospital(null, "City Emergency & Trauma Hospital", "100 Medical Square, Central District", "+1-800-108-0001", 12.97244, 77.58064, true, true, "Level 1 Trauma Center with 24/7 cardiac and pediatric emergency wings."));
        Hospital hosp2 = hospitalRepository.save(new Hospital(null, "Apollo Speciality Care Hospital", "25 Health Highway, South Park", "+1-800-108-0002", 12.96020, 77.59500, true, true, "Multi-specialty center with advanced life support and stroke units."));
        Hospital hosp3 = hospitalRepository.save(new Hospital(null, "Fortis Emergency Medical Institute", "44 Ring Road, North Zone", "+1-800-108-0003", 12.98500, 77.60500, true, true, "Equipped with rapid emergency response, ICU beds, and burn centers."));
        Hospital hosp4 = hospitalRepository.save(new Hospital(null, "Manipal Heart & Emergency Center", "88 Airport Road, East Corridor", "+1-800-108-0004", 12.95800, 77.64800, true, false, "Specialized in cardiac emergencies, respiratory support, and ICU care."));
        Hospital hosp5 = hospitalRepository.save(new Hospital(null, "St. John Memorial Hospital", "12 Lake View Road, Koramangala", "+1-800-108-0005", 12.93440, 77.62190, true, true, "Community hospital with 50 emergency beds, neonatology, and orthopedic surgery."));

        // 4. Seed Ambulances
        Ambulance amb1 = ambulanceRepository.save(new Ambulance(null, "AMB-ICU-101", AmbulanceType.ICU, driver1, 1, "Ventilator, Defibrillator, Multipara Monitor, Infusion Pumps, Portable Oxygen", AmbulanceAvailability.AVAILABLE, 12.9716, 77.5946));
        Ambulance amb2 = ambulanceRepository.save(new Ambulance(null, "AMB-BSC-102", AmbulanceType.BASIC, driver2, 2, "Stretcher, First Aid Kit, Oxygen Cylinder, Basic BP/Pulse Monitors", AmbulanceAvailability.AVAILABLE, 12.9610, 77.6010));
        Ambulance amb3 = ambulanceRepository.save(new Ambulance(null, "AMB-ALS-103", AmbulanceType.ALS, driver3, 1, "Cardiac Monitor, Defibrillator, Advanced Airway Gear, IV Kit, Suction Unit", AmbulanceAvailability.AVAILABLE, 12.9800, 77.5800));
        Ambulance amb4 = ambulanceRepository.save(new Ambulance(null, "AMB-ICU-104", AmbulanceType.ICU, driver4, 1, "Full Critical Care Support, ECMO compatible, Dual Ventilator, Blood Gas Analyzer", AmbulanceAvailability.AVAILABLE, 12.9352, 77.6245));
        Ambulance amb5 = ambulanceRepository.save(new Ambulance(null, "AMB-BSC-105", AmbulanceType.BASIC, null, 1, "First Aid, Stretcher, Oxygen delivery", AmbulanceAvailability.MAINTENANCE, 12.9500, 77.6100));

        // 5. Seed Sample Bookings
        bookingRepository.save(new Booking(null, "AMB-BK-20261001-01", patient1, amb2, driver2, hosp1, "Mark Doe", "+1-800-555-0301", "42 Elm Street, Metro City", 12.9650, 77.5980, hosp1.getName(), hosp1.getLatitude(), hosp1.getLongitude(), EmergencyType.NORMAL, AmbulanceType.BASIC, BookingStatus.COMPLETED, "Patient stabilized and admitted smoothly.", "Arrived in 8 mins", 2.3));
        bookingRepository.save(new Booking(null, "AMB-BK-20261002-02", patient2, amb3, driver3, hosp2, "Sarah Connor", "+1-800-555-0302", "108 Oak Lane, Metro City", 12.9780, 77.5840, hosp2.getName(), hosp2.getLatitude(), hosp2.getLongitude(), EmergencyType.SERIOUS, AmbulanceType.ALS, BookingStatus.COMPLETED, "Accidental fracture, transported safely.", "Arrived in 6 mins", 3.1));

        // 6. Seed Notifications
        notificationRepository.save(new Notification(null, patient1, null, "Booking Completed", "Your ambulance trip AMB-BK-20261001-01 has been marked completed. Thank you for using our service.", NotificationType.STATUS, true));
        notificationRepository.save(new Notification(null, patient1, null, "System Welcome", "Welcome to Ambulance Direct. In case of emergency, call 108 or hit Quick Book.", NotificationType.INFO, true));
        notificationRepository.save(new Notification(null, admin, null, "System Ready", "All 4 response units are online and standing by.", NotificationType.SYSTEM, false));

        // 7. Seed Emergency Contacts
        emergencyContactRepository.save(new EmergencyContact(null, patient1, "Jane Doe", "+1-800-555-0399", "Spouse"));
        emergencyContactRepository.save(new EmergencyContact(null, patient1, "Dr. Samuel Vance", "+1-800-555-0398", "Family Physician"));
        emergencyContactRepository.save(new EmergencyContact(null, patient2, "John Connor", "+1-800-555-0397", "Son"));

        log.info("Demo seed data successfully loaded into database.");
    }

    private void ensureRegionalHospitals() {
        if (!hospitalRepository.existsByName("Narayana Regional Health City")) {
            hospitalRepository.save(new Hospital(
                null,
                "Narayana Regional Health City",
                "Electronic City Phase 1, Hosur Road, South Corridor",
                "+1-800-108-0006",
                12.8250,
                77.6850,
                true,
                true,
                "Outlying tertiary multispecialty center located ~18.2 km from city center. Features 24/7 cardiac ICU and burn trauma center."
            ));
            log.info("Seeded regional hospital: Narayana Regional Health City (~18.2 km from center)");
        }

        if (!hospitalRepository.existsByName("Devanahalli Outlying Emergency Center")) {
            hospitalRepository.save(new Hospital(
                null,
                "Devanahalli Outlying Emergency Center",
                "NH 44 Airport Highway Corridor, Devanahalli",
                "+1-800-108-0007",
                13.2450,
                77.7120,
                true,
                false,
                "Highwayside emergency trauma triage station located ~32.5 km from city center."
            ));
            log.info("Seeded regional hospital: Devanahalli Outlying Emergency Center (~32.5 km from center)");
        }
    }
}
