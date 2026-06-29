package com.quickbite.backend.review.repository;

import com.quickbite.backend.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId, Pageable pageable);
    Page<Review> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);
    Optional<Review> findByOrderId(Long orderId);
    boolean existsByOrderId(Long orderId);
}
