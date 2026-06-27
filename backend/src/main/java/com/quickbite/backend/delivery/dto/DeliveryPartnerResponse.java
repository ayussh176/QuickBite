package com.quickbite.backend.delivery.dto;

import com.quickbite.backend.common.enums.DeliveryPartnerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartnerResponse {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String phone;
    private DeliveryPartnerStatus status;
    private boolean verified;
    private Double currentLatitude;
    private Double currentLongitude;
    private BigDecimal avgRating;
    private Integer totalDeliveries;
    private String profileImageUrl;
    private String drivingLicenseNumber;
    private String aadharNumber;
    private VehicleDto vehicle;
}
