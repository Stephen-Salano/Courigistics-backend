package com.courigistics.courigisticsbackend.dto.responses.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueChartData {
    private String label; // e.g., "Mon", "Jan"
    private BigDecimal amount;
}
