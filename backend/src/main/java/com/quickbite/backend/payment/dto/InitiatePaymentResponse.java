package com.quickbite.backend.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiatePaymentResponse {

    private String upiUri;
    private BigDecimal amount;
    private String orderNumber;
    private String transactionRefId;
    private String payeeUpiId;
    private String payeeName;
}
