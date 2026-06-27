package com.quickbite.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportResponse {

    private LocalDate date;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal taxAmount;
    private BigDecimal discount;
    private BigDecimal totalRevenue;
}
