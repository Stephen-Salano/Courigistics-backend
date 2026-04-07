package com.courigistics.courigisticsbackend.services.courier;

import com.courigistics.courigisticsbackend.dto.responses.delivery.TierOptionResponse;
import com.courigistics.courigisticsbackend.entities.Courier;
import com.courigistics.courigisticsbackend.entities.Delivery;
import com.courigistics.courigisticsbackend.entities.enums.PackageCategory;
import com.courigistics.courigisticsbackend.entities.enums.VehicleType;

import java.util.List;
import java.util.UUID;

/**
 * Service for matching delivery requests with available couriers
 *
 * Implements the core "matchmaking" logic:
 * - Finds available tiers based on package size and distance
 * - Filters by vehicle capacity (maxPackageCategory)
 * - Ranks couriers by proximity and type (freelancer vs employee)
 */
public interface CourierAssignmentService {

    /**
     * Determines available vehicle tiers and estimates pricing
     *
     * @param pickupLat     pickup latitude
     * @param pickupLon     pickup longitude
     * @param category      package size category
     * @param isFragile     whether fragile handling is required
     * @param distanceKm    road distance from Google Maps
     * @param city          operational city
     * @return list of available tiers with pricing and courier counts
     */
    List<TierOptionResponse> getAvailableTiers(
            double pickupLat,
            double pickupLon,
            PackageCategory category,
            boolean isFragile,
            double distanceKm,
            String city
    );

    /**
     * Formally assigns a courier to a delivery
     *
     * @param delivery  the delivery being assigned
     * @param tier      the selected vehicle tier
     * @param courierId the ID of the courier to assign
     * @return the updated Courier entity
     */
    Courier assignCourier(Delivery delivery, VehicleType tier, UUID courierId);
}
