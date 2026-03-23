package com.courigistics.courigisticsbackend.dto.requests.delivery;

import com.courigistics.courigisticsbackend.entities.enums.PackageCategory;
import jakarta.validation.constraints.NotNull;

public record DeliveryQuoteRequest(
        @NotNull(message = "Pickup latitude is required")
        Double pickupLat,

        @NotNull(message = "Pickup longitude is required")
        Double pickupLon,

        @NotNull(message = "Dropoff latitude is required")
        Double dropOffLat,

        @NotNull(message = "Dropoff longitude is required")
        Double dropOffLon,

        @NotNull(message = "Package category is required")
        PackageCategory packageCategory,

        Boolean isFragile,

        @NotNull(message = "Google Maps distance is required")
        Double googleMapsDistanceKm
) {
}
