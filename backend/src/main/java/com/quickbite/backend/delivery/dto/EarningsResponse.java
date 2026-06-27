package com.quickbite.backend.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarningsResponse {

    private BigDecimal totalEarnings;
    private Integer totalDeliveries;
}
