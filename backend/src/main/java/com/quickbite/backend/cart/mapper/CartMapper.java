package com.quickbite.backend.cart.mapper;

import com.quickbite.backend.cart.dto.CartItemResponse;
import com.quickbite.backend.cart.entity.CartItem;
import com.quickbite.backend.config.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface CartMapper {

    @Mapping(target = "foodItemId", source = "foodItem.id")
    @Mapping(target = "foodItemName", source = "foodItem.name")
    @Mapping(target = "foodItemImageUrl", expression = "java(item.getFoodItem().getImages() != null && !item.getFoodItem().getImages().isEmpty() ? " +
            "item.getFoodItem().getImages().stream().filter(img -> img.isPrimary()).map(com.quickbite.backend.menu.entity.FoodImage::getImageUrl).findFirst().orElse(item.getFoodItem().getImages().get(0).getImageUrl()) : null)")
    CartItemResponse toItemResponse(CartItem item);
}
