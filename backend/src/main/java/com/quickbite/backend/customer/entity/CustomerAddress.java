package com.quickbite.backend.customer.entity;

import com.quickbite.backend.common.BaseEntity;
import com.quickbite.backend.common.enums.AddressType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer_addresses", indexes = {
    @Index(name = "idx_cust_addr_customer_id", columnList = "customer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAddress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 30)
    private AddressType addressType;

    @Column(length = 100)
    private String label;

    @Column(name = "address_line_1", nullable = false)
    private String addressLine1;

    @Column(name = "address_line_2")
    private String addressLine2;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(name = "zip_code", nullable = false, length = 10)
    private String zipCode;

    private Double latitude;
    private Double longitude;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;
}
