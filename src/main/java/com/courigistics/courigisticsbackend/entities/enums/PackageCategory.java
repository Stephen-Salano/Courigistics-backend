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
    SMALL,
    MEDIUM,
    LARGE,
    X_LARGE
}
