package com.courigistics.courigisticsbackend.entities.enums;

public enum DeliveryStatus {
    CREATED, PENDING_PAYMENT, PAID, AT_ORIGIN_DEPOT, ASSIGNED,
    PICKED_UP, IN_TRANSIT, AT_DESTINATION_DEPOT, OUT_FOR_DELIVERY,
    DELIVERED, FAILED, CANCELLED, STORED_AT_DEPOT
}
