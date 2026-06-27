package com.quickbite.backend.menu.mapper;

import com.quickbite.backend.config.MapStructConfig;
import com.quickbite.backend.menu.dto.FoodImageDto;
import com.quickbite.backend.menu.dto.FoodItemRequest;
import com.quickbite.backend.menu.dto.FoodItemResponse;
import com.quickbite.backend.menu.entity.FoodImage;
import com.quickbite.backend.menu.entity.FoodItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface FoodItemMapper {

    @Mapping(target = "restaurantId", source = "restaurant.id")
    @Mapping(target = "categoryId", source = "category.id")
    FoodItemResponse toResponse(FoodItem item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "addOns", ignore = true)
    FoodItem toEntity(FoodItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "addOns", ignore = true)
    void updateEntityFromRequest(FoodItemRequest request, @MappingTarget FoodItem item);

    FoodImageDto toImageDto(FoodImage image);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "foodItem", ignore = true)
    FoodImage toImageEntity(FoodImageDto imageDto);

    com.quickbite.backend.menu.dto.FoodItemVariantDto toVariantDto(com.quickbite.backend.menu.entity.FoodItemVariant variant);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "foodItem", ignore = true)
    com.quickbite.backend.menu.entity.FoodItemVariant toVariantEntity(com.quickbite.backend.menu.dto.FoodItemVariantDto variantDto);

    com.quickbite.backend.menu.dto.FoodItemAddOnDto toAddOnDto(com.quickbite.backend.menu.entity.FoodItemAddOn addOn);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "foodItem", ignore = true)
    com.quickbite.backend.menu.entity.FoodItemAddOn toAddOnEntity(com.quickbite.backend.menu.dto.FoodItemAddOnDto addOnDto);
}
