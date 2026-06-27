package com.quickbite.backend.restaurant.mapper;

import com.quickbite.backend.config.MapStructConfig;
import com.quickbite.backend.restaurant.dto.RestaurantAddressDto;
import com.quickbite.backend.restaurant.dto.RestaurantRequest;
import com.quickbite.backend.restaurant.dto.RestaurantResponse;
import com.quickbite.backend.restaurant.entity.Restaurant;
import com.quickbite.backend.restaurant.entity.RestaurantAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface RestaurantMapper {

    @Mapping(target = "userId", source = "user.id")
    RestaurantResponse toResponse(Restaurant restaurant);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "avgRating", ignore = true)
    @Mapping(target = "totalReviews", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "featured", ignore = true)
    @Mapping(target = "address", ignore = true)
    Restaurant toEntity(RestaurantRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "avgRating", ignore = true)
    @Mapping(target = "totalReviews", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "featured", ignore = true)
    @Mapping(target = "address", ignore = true)
    void updateEntityFromRequest(RestaurantRequest request, @MappingTarget Restaurant restaurant);

    RestaurantAddressDto toAddressDto(RestaurantAddress address);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    RestaurantAddress toAddressEntity(RestaurantAddressDto addressDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    void updateAddressEntity(RestaurantAddressDto addressDto, @MappingTarget RestaurantAddress address);
}
