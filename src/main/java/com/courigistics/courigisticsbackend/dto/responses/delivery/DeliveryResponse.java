package com.courigistics.courigisticsbackend.dto.responses.delivery;

import com.courigistics.courigisticsbackend.entities.enums.DeliveryStatus;
import com.courigistics.courigisticsbackend.entities.enums.RouteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponse {
    private UUID id;
    private String deliveryNumber;
    private String trackingNumber;
    private DeliveryStatus status;
    private BigDecimal price;
    private RouteType routeType;
    private String pickupAddress;
    private Double pickupLat;
    private Double pickupLon;
    private String dropoffAddress;
    private Double dropOffLat;
    private Double dropOffLon;
    private String recipientName;
    private String recipientPhone;
    private LocalDateTime createdAt;
    private String courierName;
}
