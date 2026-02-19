package com.courigistics.courigisticsbackend.entities;

import com.courigistics.courigisticsbackend.entities.enums.PackageCategory;
import com.courigistics.courigisticsbackend.entities.enums.PackageType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
/**
 * Package entity representing items to be delivered
 *
 * Uses PackageCategory for vehicle capacity matching - this is separate from
 * PackageType (which indicates DOCUMENT vs PARCEL vs FRAGILE vs EXTERNAL_ORDER).
 *
 * PackageCategory tells us size/weight class for courier assignment.
 * PackageType tells us the nature of the item for handling and pricing.
 */
@Entity
@Table(name = "packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Packages {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Unique tracking number for customer tracking
     * Format: COU-PKG-YYYYMMDD-XXXX
     */
    @Column(name = "tracking_number", nullable = false, unique = true)
    private String trackingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_acc_id", unique = true, nullable = false)
    private Account senderAccount;

    @Column(name = "description", nullable = true)
    private String description;

    /**
     * Package type - nature of the shipment
     * Examples: DOCUMENT (letter/papers), PARCEL (general goods),
     *           FRAGILE (breakable), EXTERNAL_ORDER (Amazon/Jumia import)
     */
    @Column(name = "package_type")
    @Enumerated(value = EnumType.STRING)
    private PackageType packageType;

    /**
     * Package category - size/weight class for vehicle matching
     *
     * This is what determines which vehicles can carry this package.
     * When a customer selects MEDIUM_PARCEL, we automatically populate
     * weight/dimensions from the enum's preset values.
     *
     * Used in courier assignment:
     * - Filter couriers where vehicle.maxPackageCategory >= package.packageCategory
     * - BIKE couriers can carry up to SMALL_PARCEL
     * - CAR couriers can carry up to MEDIUM_PARCEL
     * - VAN couriers can carry up to LARGE_PARCEL
     * - TRUCK couriers can carry everything
     */
    @Column(name = "package_category")
    @Enumerated(value = EnumType.STRING)
    private PackageCategory packageCategory;

    /**
     * Actual or estimated weight in kilograms
     *
     * Auto-populated from packageCategory.maxWeightKg when customer picks a category.
     * Can be overridden if customer knows exact weight.
     */
    @Column(name = "weight_kg")
    private Double weightKg;

    /**
     * Package length in centimeters
     * Auto-populated from packageCategory.lengthCm
     */
    @Column(name = "length_cm")
    private Double lengthCm;

    /**
     * Package width in centimeters
     * Auto-populated from packageCategory.widthCm
     */
    @Column(name = "width_cm")
    private Double widthCm;

    /**
     * Package height in centimeters
     * Auto-populated from packageCategory.heightCm
     */
    @Column(name = "height_cm")
    private Double heightCm;

    /**
     * Indicates if package contains fragile items requiring special handling
     * Triggers fragile surcharge in pricing and may exclude bike couriers
     */
    @Column(name = "is_fragile")
    private Boolean isFragile;

    /**
     * Indicates if sender purchased insurance coverage
     * Affects pricing and liability if package is lost/damaged
     */
    @Column(name = "is_insured")
    private Boolean isInsured;

    /**
     * Declared value for insurance purposes
     * Only relevant if isInsured = true
     */
    @Column(name = "declared_value")
    private BigDecimal declaredValue;

    /**
     * Any special handling instructions from customer
     * Examples: "Call before delivery", "Leave at gate", "Handle with care"
     */
    @Column(name = "special_instructions")
    private String specialInstructions;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Lifecycle hook: auto-populate dimensions and weight from category if not already set
     *
     * This provides sensible defaults based on the category the customer selected,
     * while still allowing manual override if they know exact specs.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        populateDefaultsFromCategory();
    }

    /**
     * Auto-fills dimensions and weight from PackageCategory if not manually set
     *
     * This is called on entity creation to ensure packages always have size specs
     * for capacity validation, even if customer didn't manually enter them.
     */
    private void populateDefaultsFromCategory() {
        if (this.packageCategory != null) {
            // Only auto-fill if values are null (customer didn't override)
            if (this.weightKg == null) {
                this.weightKg = this.packageCategory.getMaxWeightKg();
            }
            if (this.lengthCm == null) {
                this.lengthCm = this.packageCategory.getLengthCm();
            }
            if (this.widthCm == null) {
                this.widthCm = this.packageCategory.getWidthCm();
            }
            if (this.heightCm == null) {
                this.heightCm = this.packageCategory.getHeightCm();
            }
        }
    }


}
