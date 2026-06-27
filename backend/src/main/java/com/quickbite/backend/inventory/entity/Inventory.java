package com.quickbite.backend.inventory.entity;

import com.quickbite.backend.common.BaseEntity;
import com.quickbite.backend.menu.entity.FoodItem;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", nullable = false, unique = true)
    private FoodItem foodItem;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "low_stock_threshold")
    @Builder.Default
    private Integer lowStockThreshold = 10;
}
