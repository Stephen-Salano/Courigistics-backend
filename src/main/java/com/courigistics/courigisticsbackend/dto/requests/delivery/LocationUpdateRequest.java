package com.courigistics.courigisticsbackend.dto.requests.delivery;

import jakarta.validation.constraints.NotNull;

public record LocationUpdateRequest(
        @NotNull(message = "Latitude is required")
        Double lat,

        @NotNull(message = "Longitude is required")
        Double lon
) {
}
