package com.quickbite.backend.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

    private String invoiceNumber;
    private LocalDateTime invoiceDate;
    private String orderNumber;
    
    // Restaurant Billing From
    private String restaurantName;
    private String restaurantAddress;
    private String restaurantGst;
    private String restaurantFssai;

    // Customer Billing To
    private String customerName;
    private String customerAddress;
    private String customerEmail;
    private String customerPhone;

    // Line items
    private List<OrderItemResponse> items;

    // Pricing breakdowns
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal taxAmount;
    private BigDecimal discount;
    private BigDecimal totalAmount;

    private String paymentMethod;
    private String transactionId;
}
