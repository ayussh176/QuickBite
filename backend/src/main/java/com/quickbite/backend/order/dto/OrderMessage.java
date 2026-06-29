package com.quickbite.backend.order.dto;

import com.quickbite.backend.common.enums.OrderStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage {
    private Long orderId;
    private String orderNumber;
    private OrderStatus status;
    private String customerEmail;
    private String restaurantEmail;
    private String riderEmail;
}
