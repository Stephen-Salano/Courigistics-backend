package com.courigistics.courigisticsbackend.config;

import com.courigistics.courigisticsbackend.entities.*;
import com.courigistics.courigisticsbackend.entities.enums.*;
import com.courigistics.courigisticsbackend.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final DepotRepository depotRepository;
    private final AccountRepository accountRepository;
    private final CourierRepository courierRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Order(1)
    @Transactional
    public void run(String... args) {
        log.info("Checking if database seeding is required...");
        seedDepots();
        seedRiders();
        logSeededRiders();
    }

    private void logSeededRiders() {
        log.info("============================================================================");
        log.info("SEEDED TEST RIDERS (COURIERS) FOR DEVELOPMENT");
        log.info("============================================================================");
        log.info("Password for all: Password123!");
        log.info("----------------------------------------------------------------------------");
        log.info("| Username             | City     | Vehicle |");
        log.info("----------------------------------------------------------------------------");
        log.info("| rider_bike           | Nairobi  | BIKE    |");
        log.info("| rider_car            | Nairobi  | CAR     |");
        log.info("| rider_van            | Nairobi  | VAN     |");
        log.info("| rider_nbo_cbd        | Nairobi  | BIKE    |");
        log.info("| rider_nbo_karen      | Nairobi  | CAR     |");
        log.info("| rider_nbo_kasarani   | Nairobi  | BIKE    |");
        log.info("| rider_nbo_embakasi   | Nairobi  | VAN     |");
        log.info("| rider_ksm_cbd        | Kisumu   | BIKE    |");
        log.info("| rider_ksm_milimani   | Kisumu   | CAR     |");
        log.info("| rider_thika_cbd      | Thika    | BIKE    |");
        log.info("| rider_thika_landless | Thika    | CAR     |");
        log.info("----------------------------------------------------------------------------");
        log.info("Map Data Public Endpoint: GET /api/v1/public/map-data");
        log.info("============================================================================");
    }

    private void seedDepots() {
        if (depotRepository.count() > 0) {
            log.debug("Depots already seeded. Skipping.");
            return;
        }

        log.info("Seeding Depots...");
        List<Depot> depots = List.of(
                createDepot("Nairobi Main Distribution Center", "NBO-MAIN", "Industrial Area, Nairobi", "Nairobi", -1.286389, 36.817223, 70.0),
                createDepot("Mombasa Main Distribution Center", "MBA-MAIN", "Port Reitz, Mombasa", "Mombasa", -4.043477, 39.668206, 30.0),
                createDepot("Kisumu Main Distribution Center", "KSM-MAIN", "Kisumu CBD, Kisumu", "Kisumu", -0.091702, 34.767956, 25.0)
        );
        depotRepository.saveAll(depots);
    }

    private Depot createDepot(String name, String code, String address, String city, Double lat, Double lon, Double radius) {
        Depot depot = new Depot();
        depot.setName(name);
        depot.setCode(code);
        depot.setAddress(address);
        depot.setCity(city);
        depot.setCountry("Kenya");
        depot.setLatitude(lat);
        depot.setLongitude(lon);
        depot.setCoverageRadiusKm(radius);
        depot.setStatus(DepotStatus.ACTIVE);
        depot.setDepotType(DepotType.STANDALONE);
        return depot;
    }

    private void seedRiders() {
        if (accountRepository.existsByUsername("rider_bike")) {
            log.debug("Test riders already seeded. Skipping.");
            return;
        }

        log.info("Seeding Test Riders...");
        String encodedPassword = passwordEncoder.encode("Password123!");

        // Nairobi Riders
        createRider("rider_bike", "bike@courigistics.com", "254700000001", "Bike", "Rider", "Nairobi", -1.2674, 36.8078, VehicleType.BIKE, "Yamaha", "DT", "KMD 123A", 20.0, 0.05);
        createRider("rider_car", "car@courigistics.com", "254700000002", "Car", "Rider", "Nairobi", -1.2892, 36.7886, VehicleType.CAR, "Toyota", "Vitz", "KCA 456B", 200.0, 0.3);
        createRider("rider_van", "van@courigistics.com", "254700000003", "Van", "Rider", "Nairobi", -1.2988, 36.8124, VehicleType.VAN, "Toyota", "TownAce", "KVD 789C", 1000.0, 5.0);
        createRider("rider_nbo_cbd", "nbo_cbd@courigistics.com", "254700000004", "CBD", "Rider", "Nairobi", -1.2841, 36.8231, VehicleType.BIKE, "Bajaj", "Boxer 150", "KMD 224A", 20.0, 0.05);
        createRider("rider_nbo_karen", "nbo_karen@courigistics.com", "254700000005", "Karen", "Rider", "Nairobi", -1.3197, 36.7050, VehicleType.CAR, "Nissan", "Note", "KCA 555C", 200.0, 0.3);
        createRider("rider_nbo_kasarani", "nbo_kasarani@courigistics.com", "254700000006", "Kasarani", "Rider", "Nairobi", -1.2217, 36.8973, VehicleType.BIKE, "TVS", "HLX", "KMD 226A", 20.0, 0.05);
        createRider("rider_nbo_embakasi", "nbo_embakasi@courigistics.com", "254700000007", "Embakasi", "Rider", "Nairobi", -1.3213, 36.9126, VehicleType.VAN, "Toyota", "Hiace", "KVD 777V", 1000.0, 5.0);

        // Kisumu Riders
        createRider("rider_ksm_cbd", "ksm_cbd@courigistics.com", "254700000008", "Kisumu", "Rider", "Kisumu", -0.1022, 34.7617, VehicleType.BIKE, "Honda", "Ace", "KMD 228A", 20.0, 0.05);
        createRider("rider_ksm_milimani", "ksm_milimani@courigistics.com", "254700000009", "Milimani", "Rider", "Kisumu", -0.1114, 34.7483, VehicleType.CAR, "Toyota", "Axio", "KCA 999D", 200.0, 0.3);

        // Thika Riders
        createRider("rider_thika_cbd", "thika_cbd@courigistics.com", "254700000010", "Thika", "Rider", "Thika", -1.0396, 37.0900, VehicleType.BIKE, "Bajaj", "Boxer 150", "KMD 230A", 20.0, 0.05);
        createRider("rider_thika_landless", "thika_landless@courigistics.com", "254700000011", "Landless", "Rider", "Thika", -1.0450, 37.1200, VehicleType.CAR, "Toyota", "Probox", "KCA 111E", 200.0, 0.3);
    }

    private void createRider(String username, String email, String phone, String first, String last, String city,
                             Double lat, Double lon, VehicleType vType, String make, String model, String plate,
                             Double capKg, Double capM3) {

        Account account = Account.builder()
                .username(username)
                .email(email)
                .phone(phone)
                .password(passwordEncoder.encode("Password123!"))
                .accountType(AccountType.COURIER)
                .enabled(true)
                .emailVerified(true)
                .accountNonLocked(true)
                .build();

        account = accountRepository.save(account);

        Courier courier = Courier.builder()
                .account(account)
                .firstName(first)
                .lastName(last)
                .status(CourierStatus.ACTIVE)
                .employmentType(EmploymentType.FREELANCER)
                .availableForAssignment(true)
                .operationalCity(city)
                .currentLat(lat)
                .currentLon(lon)
                .driversLicenseNumber("DL-" + username.toUpperCase())
                .licenseExpiryDate(LocalDate.of(2030, 1, 1))
                .pendingApproval(false)
                .build();

        courier = courierRepository.save(courier);

        Vehicles vehicle = Vehicles.builder()
                .courier(courier)
                .vehicleType(vType)
                .make(make)
                .model(model)
                .licencePlate(plate)
                .vehicleCapacityKg(capKg)
                .vehicleCapacityM3(capM3)
                .status(VehicleStatus.ACTIVE)
                .build();

        vehicleRepository.save(vehicle);
    }
}
