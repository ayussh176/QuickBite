package com.quickbite.backend.menu.repository;

import com.quickbite.backend.menu.entity.FoodImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodImageRepository extends JpaRepository<FoodImage, Long> {
    List<FoodImage> findByFoodItemId(Long foodItemId);
    void deleteByFoodItemId(Long foodItemId);
}
