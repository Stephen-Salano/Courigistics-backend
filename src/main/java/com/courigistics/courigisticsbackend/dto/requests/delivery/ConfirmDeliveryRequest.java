package com.courigistics.courigisticsbackend.dto.requests.delivery;

import com.courigistics.courigisticsbackend.entities.enums.VehicleType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConfirmDeliveryRequest(
        @NotNull(message = "Tier selection is required")
        VehicleType selectedTier,

        // Optional — if customer wants to pick a specific courier from the list
        UUID preferredCourierId
) {
}
