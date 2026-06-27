package com.quickbite.backend.delivery.entity;

import com.quickbite.backend.auth.entity.User;
import com.quickbite.backend.common.BaseEntity;
import com.quickbite.backend.common.enums.DeliveryPartnerStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "delivery_partners", indexes = {
    @Index(name = "idx_dp_status", columnList = "status"),
    @Index(name = "idx_dp_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPartner extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private DeliveryPartnerStatus status = DeliveryPartnerStatus.OFFLINE;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean verified = false;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "avg_rating", precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal avgRating = BigDecimal.ZERO;

    @Column(name = "total_deliveries")
    @Builder.Default
    private Integer totalDeliveries = 0;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @OneToOne(mappedBy = "deliveryPartner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Vehicle vehicle;

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        if (vehicle != null) {
            vehicle.setDeliveryPartner(this);
        }
    }
}
