package com.quickbite.backend.payment.mapper;

import com.quickbite.backend.config.MapStructConfig;
import com.quickbite.backend.payment.dto.PaymentResponse;
import com.quickbite.backend.payment.dto.UPIConfigurationRequest;
import com.quickbite.backend.payment.dto.UPIConfigurationResponse;
import com.quickbite.backend.payment.entity.Payment;
import com.quickbite.backend.payment.entity.UPIConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface PaymentMapper {

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "restaurantName", source = "order.restaurant.name")
    PaymentResponse toResponse(Payment payment);

    @Mapping(target = "restaurantId", source = "restaurant.id")
    UPIConfigurationResponse toResponse(UPIConfiguration config);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    UPIConfiguration toEntity(UPIConfigurationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    void updateEntityFromRequest(UPIConfigurationRequest request, @MappingTarget UPIConfiguration config);
}
