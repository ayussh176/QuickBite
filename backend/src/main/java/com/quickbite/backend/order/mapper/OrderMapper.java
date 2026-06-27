package com.quickbite.backend.order.mapper;

import com.quickbite.backend.config.MapStructConfig;
import com.quickbite.backend.order.dto.OrderItemResponse;
import com.quickbite.backend.order.dto.OrderResponse;
import com.quickbite.backend.order.entity.Order;
import com.quickbite.backend.order.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface OrderMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", expression = "java(order.getCustomer() != null ? order.getCustomer().getFirstName() + \" \" + order.getCustomer().getLastName() : null)")
    @Mapping(target = "customerPhone", source = "customer.user.phone")
    @Mapping(target = "restaurantId", source = "restaurant.id")
    @Mapping(target = "restaurantName", source = "restaurant.name")
    @Mapping(target = "restaurantPhone", source = "restaurant.phone")
    @Mapping(target = "deliveryPartnerId", source = "deliveryPartner.id")
    @Mapping(target = "deliveryPartnerName", expression = "java(order.getDeliveryPartner() != null ? order.getDeliveryPartner().getFirstName() + \" \" + order.getDeliveryPartner().getLastName() : null)")
    @Mapping(target = "deliveryPartnerPhone", expression = "java(order.getDeliveryPartner() != null ? order.getDeliveryPartner().getPhone() : null)")
    @Mapping(target = "pickedUpAt", source = "picked_up_at")
    OrderResponse toResponse(Order order);

    @Mapping(target = "foodItemId", source = "foodItem.id")
    OrderItemResponse toItemResponse(OrderItem item);
}
