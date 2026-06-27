package com.quickbite.backend.coupon.mapper;

import com.quickbite.backend.config.MapStructConfig;
import com.quickbite.backend.coupon.dto.CouponRequest;
import com.quickbite.backend.coupon.dto.CouponResponse;
import com.quickbite.backend.coupon.entity.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface CouponMapper {

    @Mapping(target = "restaurantId", source = "restaurant.id")
    @Mapping(target = "restaurantName", source = "restaurant.name")
    CouponResponse toResponse(Coupon coupon);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usedCount", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    Coupon toEntity(CouponRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usedCount", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    void updateEntityFromRequest(CouponRequest request, @MappingTarget Coupon coupon);
}
