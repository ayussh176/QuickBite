package com.quickbite.backend.customer.mapper;

import com.quickbite.backend.config.MapStructConfig;
import com.quickbite.backend.customer.dto.CustomerAddressDto;
import com.quickbite.backend.customer.dto.CustomerRequest;
import com.quickbite.backend.customer.dto.CustomerResponse;
import com.quickbite.backend.customer.entity.Customer;
import com.quickbite.backend.customer.entity.CustomerAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface CustomerMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "phone", source = "user.phone")
    CustomerResponse toResponse(Customer customer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "savedRestaurants", ignore = true)
    @Mapping(target = "wishlist", ignore = true)
    void updateEntityFromRequest(CustomerRequest request, @MappingTarget Customer customer);

    CustomerAddressDto toAddressDto(CustomerAddress address);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    CustomerAddress toAddressEntity(CustomerAddressDto addressDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    void updateAddressEntityFromDto(CustomerAddressDto addressDto, @MappingTarget CustomerAddress address);
}
