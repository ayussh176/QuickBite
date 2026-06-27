package com.quickbite.backend.menu.mapper;

import com.quickbite.backend.config.MapStructConfig;
import com.quickbite.backend.menu.dto.CategoryRequest;
import com.quickbite.backend.menu.dto.CategoryResponse;
import com.quickbite.backend.menu.entity.FoodCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface FoodCategoryMapper {

    @Mapping(target = "restaurantId", source = "restaurant.id")
    CategoryResponse toResponse(FoodCategory category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "foodItems", ignore = true)
    FoodCategory toEntity(CategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "foodItems", ignore = true)
    void updateEntityFromRequest(CategoryRequest request, @MappingTarget FoodCategory category);
}
