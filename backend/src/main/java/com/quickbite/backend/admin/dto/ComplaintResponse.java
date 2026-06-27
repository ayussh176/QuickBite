package com.quickbite.backend.admin.dto;

import com.quickbite.backend.common.enums.ComplaintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Long orderId;
    private String orderNumber;
    private String subject;
    private String description;
    private ComplaintStatus status;
    private String resolutionDetails;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
}
