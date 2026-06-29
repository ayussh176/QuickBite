package com.quickbite.backend.restaurant.dto;

import com.quickbite.backend.admin.dto.RevenueReportResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantRevenueResponse {

    private long totalOrders;
    private long completedOrders;
    private long activeOrders;
    private BigDecimal grossRevenue;
    private BigDecimal platformFees;
    private BigDecimal netRevenue;
    private BigDecimal averageOrderValue;
    private List<RevenueReportResponse> dailyRevenue;
}
