package com.courigistics.courigisticsbackend.dto.responses.delivery;

import com.courigistics.courigisticsbackend.entities.enums.DeliveryStatus;
import java.time.LocalDateTime;

public record TrackingResponse(
        String deliveryNumber,
        DeliveryStatus status,
        Double currentLat,
        Double currentLon,
        Double actualDistanceKm,
        LocalDateTime estimatedArrival,
        String courierName,
        String courierPhone,
        Double courierLat,
        Double courierLon
) {
}
