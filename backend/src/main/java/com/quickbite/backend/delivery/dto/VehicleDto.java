package com.quickbite.backend.delivery.dto;

import com.quickbite.backend.common.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDto {

    private Long id;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    private String make;
    private String model;

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    private String color;
}
