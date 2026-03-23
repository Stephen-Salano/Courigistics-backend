package com.courigistics.courigisticsbackend.dto.responses.delivery;

import com.courigistics.courigisticsbackend.entities.enums.RouteType;

import java.util.List;
import java.util.UUID;

public record DeliveryCreationResponse (
        UUID deliveryId,
        String deliveryNumber,
        String trackingNumber,
        Double estimatedDistanceKm,
        RouteType routeType,
        List<TierOptionResponse> availableTiers
) {
}
