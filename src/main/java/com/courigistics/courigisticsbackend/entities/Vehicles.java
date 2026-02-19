package com.courigistics.courigisticsbackend.entities;

import com.courigistics.courigisticsbackend.entities.enums.PackageCategory;
import com.courigistics.courigisticsbackend.entities.enums.VehicleStatus;
import com.courigistics.courigisticsbackend.entities.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Vehicle entity representing courier transportation with capacity specifications
 *
 * Uses PackageCategory to define what size packages this vehicle can carry.
 * This enables intelligent courier assignment - bikes can't carry furniture,
 * but can efficiently deliver documents and small parcels.
 */

@Builder
@Entity
@Table(name = "vehicles", indexes = {
        @Index(name = "idx_vehicle_courier", columnList = "courier_id"),
        @Index(name = "idx_depot_id", columnList = "depot_id"),
        @Index(name = "idx_license_plate", columnList = "license_plate")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Vehicles {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "courier_id", nullable = false, unique = true)
    @OneToOne(fetch = FetchType.LAZY)
    private Courier courier;

    @JoinColumn(name = "depot_id", nullable = true)
    @ManyToOne(fetch = FetchType.LAZY)
    private Depot depot;

    /**
     * Vehicle type determines default capacity category
     *
     * Auto-mapped at registration:
     * BIKE → SMALL_PARCEL (can carry documents and small items)
     * CAR → MEDIUM_PARCEL (can carry up to microwave-sized items)
     * VAN → LARGE_PARCEL (can carry TV-sized items)
     * TRUCK → FURNITURE (can carry everything)
     */
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    @Column(name = "make", nullable = false)
    private String make;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "production_year")
    private String manufactureYear;

    @Column(name = "color")
    private String vehicleColor;

    @Column(name = "license_plate", unique = true)
    private String licencePlate;

    @Column(name = "chassis_number", unique = true)
    private String chassisNumber;

    /**
     * Vehicle weight capacity in kilograms
     *
     * Reference values by type:
     * BIKE: ~15kg
     * CAR: ~80kg
     * VAN: ~500kg
     * TRUCK: ~3000kg
     */
    @Column(name = "capacity_kg", nullable = false)
    private Double vehicleCapacityKg;

    /**
     * Vehicle volume capacity in cubic meters
     *
     * Reference values by type:
     * BIKE: ~0.05m³ (backpack/small box)
     * CAR: ~0.3m³ (trunk space)
     * VAN: ~5m³ (cargo area)
     * TRUCK: ~30m³ (full bed)
     */
    @Column(name = "capacity_m3", nullable = false)
    private Double vehicleCapacityM3;

    /**
     * Maximum package category this vehicle can carry
     *
     * This is the key field for courier assignment logic:
     *
     * Assignment rule:
     *   package.packageCategory <= vehicle.maxPackageCategory
     *
     * Examples:
     * - BIKE (maxPackageCategory = SMALL_PARCEL) can deliver:
     *   ✓ DOCUMENT (letters)
     *   ✓ SMALL_PARCEL (shoebox items)
     *   ✗ MEDIUM_PARCEL (too big)
     *
     * - CAR (maxPackageCategory = MEDIUM_PARCEL) can deliver:
     *   ✓ DOCUMENT, SMALL_PARCEL, MEDIUM_PARCEL
     *   ✗ LARGE_PARCEL (won't fit in car trunk)
     *
     * - TRUCK (maxPackageCategory = FURNITURE) can deliver everything
     *
     * Set automatically from vehicleType during registration,
     * but can be overridden (e.g., small car might only handle SMALL_PARCEL).
     */
    @Enumerated(value = EnumType.STRING)
    @Column(name = "max_package_category")
    private PackageCategory maxPackageCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private VehicleStatus status = VehicleStatus.ACTIVE;

    @Column(name = "insurance_expiry_date")
    private String insuranceExpiryDate;

    @Column(name = "vehicle_insurance_number")
    private String insuranceNumber;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Lifecycle hook: auto-set maxPackageCategory from vehicleType if not already set
     *
     * This ensures every vehicle has a capacity category, even if courier
     * registration didn't explicitly set it. Simplifies frontend UX.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();

        // Auto-assign maxPackageCategory based on vehicleType if not set
        if (this.maxPackageCategory == null && this.vehicleType != null) {
            this.maxPackageCategory = getDefaultCategoryForVehicleType(this.vehicleType);
        }
    }

    /**
     * Maps vehicle types to default package categories
     *
     * These are realistic capacity limits based on typical vehicle sizes:
     *
     * @param vehicleType the type of vehicle
     * @return the maximum package category it can carry
     */
    private PackageCategory getDefaultCategoryForVehicleType(VehicleType vehicleType) {
        return switch (vehicleType) {
            case BIKE -> PackageCategory.SMALL_PARCEL;   // Backpack/box capacity
            case CAR -> PackageCategory.MEDIUM_PARCEL;    // Trunk capacity
            case VAN -> PackageCategory.LARGE_PARCEL;     // Cargo area capacity
            case TRUCK -> PackageCategory.FURNITURE;      // Full bed capacity
        };
    }

    /**
     * Checks if this vehicle can carry a package of the given category
     *
     * Uses enum ordinal comparison - categories are ordered small to large.
     *
     * @param category the package category to check
     * @return true if this vehicle can carry packages of that category
     */
    public boolean canCarry(PackageCategory category) {
        return category.canBeCarriedBy(this.maxPackageCategory);
    }

}
