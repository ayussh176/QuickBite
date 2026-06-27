package com.quickbite.backend.restaurant.repository;

import com.quickbite.backend.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Optional<Restaurant> findByUserId(Long userId);
    Optional<Restaurant> findByUserEmail(String email);
    Optional<Restaurant> findBySlug(String slug);
    boolean existsByName(String name);
}
