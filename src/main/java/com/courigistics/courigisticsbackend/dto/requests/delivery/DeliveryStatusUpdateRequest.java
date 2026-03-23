package com.courigistics.courigisticsbackend.dto.requests.delivery;

import com.courigistics.courigisticsbackend.entities.enums.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

public record DeliveryStatusUpdateRequest(
        @NotNull(message = "New status is required")
        DeliveryStatus newStatus,

        // Optional note — e.g. reason for failure or cancellation
        String note
) {
}
