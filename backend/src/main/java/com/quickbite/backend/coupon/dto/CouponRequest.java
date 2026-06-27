package com.quickbite.backend.coupon.dto;

import com.quickbite.backend.common.enums.CouponType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponRequest {

    @NotBlank(message = "Coupon code is required")
    private String code;

    private String description;

    @NotNull(message = "Coupon type is required")
    private CouponType couponType;

    @NotNull(message = "Coupon value is required")
    @DecimalMin(value = "0.01", message = "Value must be positive")
    private BigDecimal value;

    private BigDecimal minOrderAmount;

    private BigDecimal maxDiscount;

    private Integer usageLimit;

    @NotNull(message = "Start date is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Expiry date is required")
    private LocalDateTime validTo;

    private Boolean active;

    private Long restaurantId; // Null for global coupons
}
