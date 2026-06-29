package com.quickbite.backend.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantAnalyticsResponse {

    private long menuItems;
    private long availableMenuItems;
    private long categories;
    private long lowStockItems;
    private long activeCoupons;
    private long totalOrders;
    private long activeOrders;
    private List<TopSellingItemResponse> topSellingItems;
}
