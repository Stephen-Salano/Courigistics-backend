package com.courigistics.courigisticsbackend.dto.requests.delivery;

import com.courigistics.courigisticsbackend.entities.enums.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

public record DeliveryMilestoneRequest(
        @NotNull(message = "Status is required")
        DeliveryStatus status,

        @NotNull(message = "Distance covered is required")
        Double distanceKm,

        @NotNull(message = "Current latitude is required")
        Double currentLat,

        @NotNull(message = "Current longitude is required")
        Double currentLon
) {
}
