package com.quickbite.backend.menu.entity;

import com.quickbite.backend.common.BaseEntity;
import com.quickbite.backend.common.enums.FoodType;
import com.quickbite.backend.restaurant.entity.Restaurant;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "food_items", indexes = {
    @Index(name = "idx_food_item_restaurant", columnList = "restaurant_id"),
    @Index(name = "idx_food_item_category", columnList = "category_id"),
    @Index(name = "idx_food_item_type", columnList = "food_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private FoodCategory category;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(unique = true, length = 250)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "discounted_price", precision = 10, scale = 2)
    private BigDecimal discountedPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "food_type", nullable = false, length = 30)
    private FoodType foodType;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private boolean available = true;

    @Column(name = "is_bestseller", nullable = false)
    @Builder.Default
    private boolean bestseller = false;

    @Column(name = "preparation_time")
    private Integer preparationTime; // in minutes

    @OneToMany(mappedBy = "foodItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<FoodImage> images = new ArrayList<>();

    public void addImage(FoodImage image) {
        images.add(image);
        image.setFoodItem(this);
    }

    public void removeImage(FoodImage image) {
        images.remove(image);
        image.setFoodItem(null);
    }
}
