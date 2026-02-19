package com.courigistics.courigisticsbackend.entities;

import com.courigistics.courigisticsbackend.entities.enums.CourierStatus;
import com.courigistics.courigisticsbackend.entities.enums.EmploymentType;
import com.courigistics.courigisticsbackend.entities.enums.PaymentType;
import com.courigistics.courigisticsbackend.utils.GeoUtils;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Courier entity representing delivery personnel with real-time location tracking
 *
 * Tracks courier position for Uber-style assignment where nearby available couriers are matched
 * with delivery requests based on proximity and vehicle capacity
 */
@Entity
@Getter
@Setter
@Table(name = "couriers", indexes = {
        @Index(name = "idx_courier_account", columnList = "account_id"),
        @Index(name = "idx_courier_depot", columnList = "depot_id"),
        @Index(name = "idx_courier_status", columnList = "status"),
        @Index(name = "idx_courier_employee_id", columnList = "employee_id"),
        @Index(name = "idx_courier_operational_city", columnList =  "operational_city"),
        @Index(name = "idx_courier_available", columnList = "available_for_assignment")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Courier {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "profile_image", unique = true)
    private String profileImageUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", unique = true, nullable = false)
    private Account account;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CourierStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id", nullable = true)
    private Depot depot;

    @Column(name = "employee_id", unique = true)
    private String employeeId;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    @Column(name = "national_id", unique = true)
    private String nationalId;

    @Column(name = "drivers_license_number", unique = true, nullable = false)
    private String driversLicenseNumber;

    @Column(name = "license_expiry_date", nullable = false)
    private LocalDate licenseExpiryDate;

    @Column(name = "pending_approval", nullable = false)
    private Boolean pendingApproval = true;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Account approvedBy;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Column(name = "base_salary")
    private BigDecimal baseSalary;

    @Column(name = "commission_rate")
    private BigDecimal commissionRate;

    @Column(name = "max_weight_per_route_kg")
    private Double maxWeightPerRoute = 100.0;

    @Column(name = "max_deliveries_per_route")
    private Integer maxDeliveriesPerDay = 20;

    /**
     * Operational city for city-level filtering before proximity checks
     *
     * Examples: "Nairobi", "Mombasa", "Kisumu"
     *
     * Used to quickly filter couriers to the relevant city before running
     * expensive PostGIS distance calculations. This prevents assigning a
     * Nairobi courier to a Mombasa delivery even if distance query fails.
     */
    @Column(name = "operational_city")
    private String operationalCity;

    /**
     * Current latitude for real-time position tracking
     *
     * updated when courier moves (via frontend location API or GPS)
     * Used to populate currentLocation Point field for spatial queries
     */
    @Column(name = "curent_lat")
    private Double currentLat;

    /**
     * Current longitude for real-time position tracking
     *
     * Updated when courier moves (via frontend location API or GPS)
     * Used to populate currentLocation Point field for spatial queries
     */
    @Column(name = "current_lon")
    private Double currentLon;

    /**
     * PostGIS geography point representing courier's current position
     *
     * Automatically synchronized with currentLat/currentLon via @PreUpdate.
     *
     * Enables Uber-style proximity queries:
     * - Find couriers within 10km of pickup location
     * - Sort couriers by distance to customer
     * - Filter by vehicle capacity + operational city + proximity
     *
     * Nullable for H2 test compatibility and when courier hasn't shared location yet.
     */
    @Column(name = "current_location", columnDefinition = "geography(Point, 4326)", nullable = true)
    private Point currentLocation;

    @Column(name = "available_for_assignment", nullable = false)
    @Builder.Default
    private Boolean availableForAssignment = true;

    @Column(name = "hired_at")
    private LocalDateTime hiredAt;

    @Column(name = "fired_at")
    private LocalDateTime firedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        populateCurrentLocationFromCoordinates();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        populateCurrentLocationFromCoordinates();
    }

    private void populateCurrentLocationFromCoordinates(){
        this.currentLocation = GeoUtils.createPointSafe(this.currentLat, this.currentLon);
    }

}
