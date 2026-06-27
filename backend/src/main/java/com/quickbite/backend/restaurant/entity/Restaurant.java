package com.quickbite.backend.restaurant.entity;

import com.quickbite.backend.auth.entity.User;
import com.quickbite.backend.common.BaseEntity;
import com.quickbite.backend.common.enums.RestaurantStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "restaurants", indexes = {
    @Index(name = "idx_restaurant_slug", columnList = "slug"),
    @Index(name = "idx_restaurant_status", columnList = "status"),
    @Index(name = "idx_restaurant_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 250)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cuisine_type")
    private String cuisineType;

    @Column(length = 20)
    private String phone;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private RestaurantStatus status = RestaurantStatus.PENDING_APPROVAL;

    @Column(name = "fssai_license", length = 50)
    private String fssaiLicense;

    @Column(name = "gst_number", length = 20)
    private String gstNumber;

    @Column(name = "avg_rating", precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal avgRating = BigDecimal.ZERO;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(name = "min_order_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "estimated_delivery_time")
    private Integer estimatedDeliveryTime; // in minutes

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private boolean featured = false;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @OneToOne(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private RestaurantAddress address;

    public void setAddress(RestaurantAddress address) {
        this.address = address;
        if (address != null) {
            address.setRestaurant(this);
        }
    }
}
