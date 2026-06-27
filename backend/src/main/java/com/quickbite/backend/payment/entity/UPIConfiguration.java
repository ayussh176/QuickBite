package com.quickbite.backend.payment.entity;

import com.quickbite.backend.common.BaseEntity;
import com.quickbite.backend.restaurant.entity.Restaurant;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "upi_configurations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UPIConfiguration extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "upi_id", nullable = false, length = 100)
    private String upiId;

    @Column(name = "provider_name", length = 50)
    private String providerName;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
