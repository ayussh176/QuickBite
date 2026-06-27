package com.quickbite.backend.menu.entity;

import com.quickbite.backend.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "food_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private boolean primary = false;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
}
