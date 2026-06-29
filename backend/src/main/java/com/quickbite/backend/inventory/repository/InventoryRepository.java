package com.quickbite.backend.inventory.repository;

import com.quickbite.backend.inventory.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByFoodItemId(Long foodItemId);

    @Query("SELECT i FROM Inventory i JOIN FETCH i.foodItem f WHERE f.restaurant.id = :restaurantId AND i.quantity <= i.lowStockThreshold")
    Page<Inventory> findLowStockItemsByRestaurantId(Long restaurantId, Pageable pageable);
    
    @Query("SELECT i FROM Inventory i JOIN FETCH i.foodItem f WHERE f.restaurant.id = :restaurantId")
    Page<Inventory> findByRestaurantId(Long restaurantId, Pageable pageable);

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.foodItem.restaurant.id = :restaurantId AND i.quantity <= i.lowStockThreshold")
    long countLowStockItemsByRestaurantId(Long restaurantId);
}
