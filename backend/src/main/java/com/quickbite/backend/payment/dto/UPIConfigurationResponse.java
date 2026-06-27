package com.quickbite.backend.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UPIConfigurationResponse {

    private Long id;
    private Long restaurantId;
    private String upiId;
    private String providerName;
    private boolean isDefault;
    private boolean isActive;
}
