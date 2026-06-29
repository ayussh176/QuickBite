package com.quickbite.backend.payment.dto;

import com.quickbite.backend.common.enums.PaymentStatus;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMessage {
    private Long paymentId;
    private Long orderId;
    private String orderNumber;
    private PaymentStatus status;
    private BigDecimal amount;
    private String customerEmail;
}
