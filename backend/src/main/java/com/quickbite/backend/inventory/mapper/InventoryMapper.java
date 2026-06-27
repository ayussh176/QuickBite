package com.quickbite.backend.inventory.mapper;

import com.quickbite.backend.config.MapStructConfig;
import com.quickbite.backend.inventory.dto.InventoryResponse;
import com.quickbite.backend.inventory.entity.Inventory;
import com.quickbite.backend.menu.mapper.FoodItemMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class, uses = {FoodItemMapper.class})
public interface InventoryMapper {

    @Mapping(target = "foodItem", source = "foodItem")
    InventoryResponse toResponse(Inventory inventory);
}
