package com.courigistics.courigisticsbackend.dto.responses.delivery;

import com.courigistics.courigisticsbackend.entities.enums.VehicleType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TierOptionResponse(
        VehicleType tier,
        String tierLabel,          // e.g. "Bike", "Car", "Van", "Truck"
        BigDecimal estimatedPrice,
        int availableCourierCount,
        List<CourierSummary> topCouriers
) {
    public record CourierSummary(
            UUID courierId,
            String firstName,
            Double rating,
            Double distanceKm
    ) {}
}
