package com.quickbite.backend.cart.entity;

import com.quickbite.backend.common.BaseEntity;
import com.quickbite.backend.menu.entity.FoodItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items", uniqueConstraints = {
    @UniqueConstraint(name = "uq_cart_food_item", columnNames = {"cart_id", "food_item_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "special_instructions", length = 500)
    private String specialInstructions;

    public void updateQuantity(int newQuantity) {
        this.quantity = newQuantity;
        this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(newQuantity));
        if (this.cart != null) {
            this.cart.recalculateTotal();
        }
    }
}
