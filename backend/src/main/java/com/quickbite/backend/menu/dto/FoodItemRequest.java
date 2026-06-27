package com.quickbite.backend.menu.dto;

import com.quickbite.backend.common.enums.FoodType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodItemRequest {

    @NotBlank(message = "Food item name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    private BigDecimal price;

    private BigDecimal discountedPrice;

    @NotNull(message = "Food type is required")
    private FoodType foodType;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private Boolean available;
    private Boolean bestseller;
    private Integer preparationTime;

    private List<FoodImageDto> images;
}
