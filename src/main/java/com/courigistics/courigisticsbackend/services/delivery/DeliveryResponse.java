package com.courigistics.courigisticsbackend.services.delivery;

import com.courigistics.courigisticsbackend.entities.enums.DeliveryStatus;
import com.courigistics.courigisticsbackend.entities.enums.RouteType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Placeholder DTO for delivery response.
 * This will be expanded as we implement more status tracking features.
 */
public record DeliveryResponse(
        UUID id,
        String deliveryNumber,
        String trackingNumber,
        DeliveryStatus status,
        BigDecimal price,
        RouteType routeType,
        String pickupAddress,
        String dropoffAddress,
        LocalDateTime createdAt,
        String courierName
) {
}
