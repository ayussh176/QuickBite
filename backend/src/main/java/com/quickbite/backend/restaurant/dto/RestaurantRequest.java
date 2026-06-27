package com.quickbite.backend.restaurant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class RestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    private String name;

    private String description;
    private String cuisineType;
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    private String fssaiLicense;
    private String gstNumber;

    private LocalTime openingTime;
    private LocalTime closingTime;

    private BigDecimal minOrderAmount;
    private BigDecimal deliveryFee;
    private Integer estimatedDeliveryTime;

    private String profileImageUrl;
    private String coverImageUrl;

    @NotNull(message = "Restaurant address is required")
    @Valid
    private RestaurantAddressDto address;
}
