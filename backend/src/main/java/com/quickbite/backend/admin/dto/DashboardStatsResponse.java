package com.quickbite.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private long totalUsers;
    private long totalRestaurants;
    private long totalDeliveryPartners;
    private long totalOrders;
    private BigDecimal totalRevenue;
    
    private long pendingRestaurantApprovals;
    private long pendingRiderVerifications;
    private long pendingComplaints;
}
