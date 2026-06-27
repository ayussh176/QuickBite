package com.quickbite.backend.order.dto;

import com.quickbite.backend.common.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {

    @NotNull(message = "Delivery address ID is required")
    private Long deliveryAddressId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String specialInstructions;
}
