package com.quickbite.backend.review.mapper;

import com.quickbite.backend.config.MapStructConfig;
import com.quickbite.backend.review.dto.ReviewResponse;
import com.quickbite.backend.review.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface ReviewMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", expression = "java(review.getCustomer() != null ? review.getCustomer().getFirstName() + \" \" + review.getCustomer().getLastName() : null)")
    @Mapping(target = "restaurantId", source = "restaurant.id")
    @Mapping(target = "restaurantName", source = "restaurant.name")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    ReviewResponse toResponse(Review review);
}
