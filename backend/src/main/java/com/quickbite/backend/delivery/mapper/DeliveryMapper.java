package com.quickbite.backend.delivery.mapper;

import com.quickbite.backend.config.MapStructConfig;
import com.quickbite.backend.delivery.dto.DeliveryPartnerRequest;
import com.quickbite.backend.delivery.dto.DeliveryPartnerResponse;
import com.quickbite.backend.delivery.dto.VehicleDto;
import com.quickbite.backend.delivery.entity.DeliveryPartner;
import com.quickbite.backend.delivery.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface DeliveryMapper {

    @Mapping(target = "userId", source = "user.id")
    DeliveryPartnerResponse toResponse(DeliveryPartner partner);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "currentLatitude", ignore = true)
    @Mapping(target = "currentLongitude", ignore = true)
    @Mapping(target = "avgRating", ignore = true)
    @Mapping(target = "totalDeliveries", ignore = true)
    @Mapping(target = "drivingLicenseNumber", ignore = true)
    @Mapping(target = "aadharNumber", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    void updateEntityFromRequest(DeliveryPartnerRequest request, @MappingTarget DeliveryPartner partner);

    VehicleDto toVehicleDto(Vehicle vehicle);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deliveryPartner", ignore = true)
    Vehicle toVehicleEntity(VehicleDto vehicleDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deliveryPartner", ignore = true)
    void updateVehicleEntityFromDto(VehicleDto vehicleDto, @MappingTarget Vehicle vehicle);
}
