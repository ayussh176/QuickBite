package com.quickbite.backend.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopSellingItemResponse {

    private Long foodItemId;
    private String foodItemName;
    private long quantitySold;
    private BigDecimal revenue;
}
