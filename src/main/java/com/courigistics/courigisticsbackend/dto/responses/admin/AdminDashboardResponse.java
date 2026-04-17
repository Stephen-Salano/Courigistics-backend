package com.courigistics.courigisticsbackend.dto.responses.admin;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AdminDashboardResponse {
    private SummaryDTO summary;
    private List<RevenueChartData> revenueChart;
    private List<RecentDeliveryDTO> recentDeliveries;

    @Data
    @Builder
    public static class SummaryDTO {
        private BigDecimal totalRevenue;
        private Double totalDistance;
        private long activeCouriers;
        private long pendingApprovals;
        private long ongoingDeliveries;
    }

    @Data
    @Builder
    public static class RecentDeliveryDTO {
        private String id;
        private String deliveryNumber;
        private String status;
        private String customerName;
        private String courierName;
        private BigDecimal amount;
    }
}
