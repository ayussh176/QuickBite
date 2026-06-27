package com.quickbite.backend.menu.repository;

import com.quickbite.backend.common.enums.FoodType;
import com.quickbite.backend.menu.entity.FoodItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long>, JpaSpecificationExecutor<FoodItem> {
    List<FoodItem> findByRestaurantId(Long restaurantId);
    List<FoodItem> findByCategoryId(Long categoryId);
    Optional<FoodItem> findByRestaurantIdAndSlug(Long restaurantId, String slug);
    boolean existsByRestaurantIdAndName(Long restaurantId, String name);
    
    // Pageable search queries
    Page<FoodItem> findByRestaurantIdAndAvailable(Long restaurantId, boolean available, Pageable pageable);
    Page<FoodItem> findByRestaurantIdAndCategoryIdAndAvailable(Long restaurantId, Long categoryId, boolean available, Pageable pageable);
    Page<FoodItem> findByRestaurantIdAndFoodTypeAndAvailable(Long restaurantId, FoodType foodType, boolean available, Pageable pageable);
}
