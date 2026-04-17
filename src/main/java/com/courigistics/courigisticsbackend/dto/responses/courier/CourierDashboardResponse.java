package com.courigistics.courigisticsbackend.dto.responses.courier;

import lombok.Builder;

@Builder
public record CourierDashboardResponse(
        long completedToday,
        long activeDeliveries,
        long pendingOffers,
        Double totalEarningsToday,
        Double averageRating
) {
}
