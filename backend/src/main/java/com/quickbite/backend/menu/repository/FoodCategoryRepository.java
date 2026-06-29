package com.quickbite.backend.menu.repository;

import com.quickbite.backend.menu.entity.FoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodCategoryRepository extends JpaRepository<FoodCategory, Long> {
    List<FoodCategory> findByRestaurantIdOrderBySortOrderAsc(Long restaurantId);
    List<FoodCategory> findByRestaurantIdAndActiveTrueOrderBySortOrderAsc(Long restaurantId);
    Optional<FoodCategory> findByRestaurantIdAndName(Long restaurantId, String name);
    Optional<FoodCategory> findByRestaurantIdAndNameIgnoreCase(Long restaurantId, String name);
    boolean existsByRestaurantIdAndName(Long restaurantId, String name);
    long countByRestaurantIdAndActiveTrue(Long restaurantId);
}
