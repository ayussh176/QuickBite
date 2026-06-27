package com.quickbite.backend.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycRequest {

    @NotBlank(message = "Driving license number is required")
    private String drivingLicenseNumber;

    @NotBlank(message = "Aadhar number is required")
    private String aadharNumber;
}
