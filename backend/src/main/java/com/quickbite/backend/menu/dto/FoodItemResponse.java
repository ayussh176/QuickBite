package com.quickbite.backend.menu.dto;

import com.quickbite.backend.common.enums.FoodType;
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
public class FoodItemResponse {

    private Long id;
    private Long restaurantId;
    private Long categoryId;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private FoodType foodType;
    private boolean available;
    private boolean bestseller;
    private Integer preparationTime;
    private List<FoodImageDto> images;
    private List<FoodItemVariantDto> variants;
    private List<FoodItemAddOnDto> addOns;
}
