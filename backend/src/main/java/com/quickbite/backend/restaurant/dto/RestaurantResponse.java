package com.quickbite.backend.restaurant.dto;

import com.quickbite.backend.common.enums.RestaurantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {

    private Long id;
    private Long userId;
    private String name;
    private String slug;
    private String description;
    private String cuisineType;
    private String phone;
    private String email;
    private RestaurantStatus status;
    private String fssaiLicense;
    private String gstNumber;
    private BigDecimal avgRating;
    private Integer totalReviews;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private BigDecimal minOrderAmount;
    private BigDecimal deliveryFee;
    private Integer estimatedDeliveryTime;
    private boolean active;
    private boolean featured;
    private String profileImageUrl;
    private String coverImageUrl;
    private RestaurantAddressDto address;
}
