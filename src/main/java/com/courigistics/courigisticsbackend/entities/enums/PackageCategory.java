package com.courigistics.courigisticsbackend.entities.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Package categories with realistic weight and volume specifications
 *
 * These categories solve the UX problem of asking users to measure/weigh packages.
 * Instead, they pick a category and we infer the specs. Each category maps to
 * vehicle types that can carry it, enabling smart courier assignment.
 *
 * Design philosophy:
 * - DOCUMENT: Letters, thin envelopes - any vehicle can carry
 * - SMALL_PARCEL: Shoebox-sized items - bikes can handle
 * - MEDIUM_PARCEL: Microwave-sized items - needs car trunk
 * - LARGE_PARCEL: TV-sized items - requires van
 * - FRAGILE: Similar to medium but needs extra care
 * - FURNITURE: Bulky items - truck required
 *
 * Vehicle mapping (defined in vehicle registration):
 * BIKE → max SMALL_PARCEL
 * CAR → max MEDIUM_PARCEL
 * VAN → max LARGE_PARCEL
 * TRUCK → can carry everything
 */
@AllArgsConstructor
@Getter
public enum PackageCategory {
    /**
     * Documents, letters, thin folders
     * Examples: Contracts, certificates, passports
     *
     * Weight: up to 0.5kg
     * Dimensions: A4 size (30cm x 30cm x 5cm)
     * Max volume: 0.0045 cubic meters
     */
    DOCUMENT(
            0.5,      // maxWeightKg
            0.0045,   // maxVolumeM3
            30.0,     // lengthCm
            30.0,     // widthCm
            5.0       // heightCm
    ),

    /**
     * Small parcels - shoebox sized
     * Examples: Shoes, books, small electronics
     *
     * Weight: up to 5kg
     * Dimensions: 40cm x 30cm x 20cm
     * Max volume: 0.024 cubic meters
     */
    SMALL_PARCEL(
            5.0,
            0.024,
            40.0,
            30.0,
            20.0
    ),

    /**
     * Medium parcels - microwave sized
     * Examples: Small appliances, clothing boxes
     *
     * Weight: up to 15kg
     * Dimensions: 60cm x 40cm x 40cm
     * Max volume: 0.096 cubic meters
     */
    MEDIUM_PARCEL(
            15.0,
            0.096,
            60.0,
            40.0,
            40.0
    ),

    /**
     * Large parcels - TV sized
     * Examples: Large electronics, multiple boxes
     *
     * Weight: up to 30kg
     * Dimensions: 100cm x 60cm x 50cm
     * Max volume: 0.3 cubic meters
     */
    LARGE_PARCEL(
            30.0,
            0.3,
            100.0,
            60.0,
            50.0
    ),

    /**
     * Fragile items requiring special handling
     * Examples: Glassware, artwork, delicate electronics
     *
     * Same specs as MEDIUM_PARCEL but triggers fragile surcharge
     * and may exclude bike couriers in assignment logic
     *
     * Weight: up to 10kg (lighter due to fragility)
     * Dimensions: 50cm x 40cm x 40cm
     * Max volume: 0.08 cubic meters
     */
    FRAGILE(
            10.0,
            0.08,
            50.0,
            40.0,
            40.0
    ),

    /**
     * Furniture and oversized items
     * Examples: Chairs, tables, large appliances
     *
     * Weight: up to 50kg
     * Dimensions: 150cm x 100cm x 80cm
     * Max volume: 1.2 cubic meters
     */
    FURNITURE(
            50.0,
            1.2,
            150.0,
            100.0,
            80.0
    );

    /**
     * Maximum weight this category supports in kilograms
     */
    private final double maxWeightKg;

    /**
     * Maximum volume this category supports in cubic meters
     */
    private final double maxVolumeM3;

    /**
     * Reference length dimension in centimeters
     * Used to auto-fill package dimensions when user selects category
     */
    private final double lengthCm;

    /**
     * Reference width dimension in centimeters
     */
    private final double widthCm;

    /**
     * Reference height dimension in centimeters
     */
    private final double heightCm;

    /**
     * Checks if this category can fit in a vehicle with given capacity
     *
     * @param vehicleMaxWeightKg  the vehicle's weight capacity in kg
     * @param vehicleMaxVolumeM3  the vehicle's volume capacity in m³
     * @return true if this package can fit in that vehicle
     */
    public boolean fitsInVehicle(double vehicleMaxWeightKg, double vehicleMaxVolumeM3) {
        return this.maxWeightKg <= vehicleMaxWeightKg
                && this.maxVolumeM3 <= vehicleMaxVolumeM3;
    }

    /**
     * Checks if this category requires a vehicle at or above a certain level
     *
     * Uses enum ordinal comparison - categories are ordered from smallest to largest.
     *
     * Example:
     * - LARGE_PARCEL.requiresVehicleCategory(MEDIUM_PARCEL) = false (large > medium)
     * - MEDIUM_PARCEL.requiresVehicleCategory(LARGE_PARCEL) = true (medium fits in large)
     *
     * @param vehicleMaxCategory  the maximum category a vehicle can carry
     * @return true if this package can be carried by that vehicle category
     */
    public boolean canBeCarriedBy(PackageCategory vehicleMaxCategory) {
        return this.ordinal() <= vehicleMaxCategory.ordinal();
    }
}
