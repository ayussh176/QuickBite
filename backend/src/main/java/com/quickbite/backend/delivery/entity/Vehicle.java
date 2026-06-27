package com.quickbite.backend.delivery.entity;

import com.quickbite.backend.common.BaseEntity;
import com.quickbite.backend.common.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_partner_id", nullable = false, unique = true)
    private DeliveryPartner deliveryPartner;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 30)
    private VehicleType vehicleType;

    @Column(length = 50)
    private String make;

    @Column(length = 50)
    private String model;

    @Column(name = "registration_number", nullable = false, unique = true, length = 20)
    private String registrationNumber;

    @Column(length = 30)
    private String color;
}
