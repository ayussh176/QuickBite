package com.quickbite.backend.menu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodItemVariantDto {

    private Long id;

    @NotBlank(message = "Variant name is required")
    private String name;

    @NotNull(message = "Variant price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Variant price cannot be negative")
    private BigDecimal price;

    private Boolean available;
}
