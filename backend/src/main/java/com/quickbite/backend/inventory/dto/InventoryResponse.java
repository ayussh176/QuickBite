package com.quickbite.backend.inventory.dto;

import com.quickbite.backend.menu.dto.FoodItemResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Long id;
    private FoodItemResponse foodItem;
    private Integer quantity;
    private Integer lowStockThreshold;
}
