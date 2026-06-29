package com.quickbite.backend.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private Long restaurantId;
    private String restaurantName;
    private Long orderId;
    private String orderNumber;
    private Integer rating;
    private String comment;
    private String reply;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;
}
