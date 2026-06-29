package com.quickbite.backend.order.repository;

import com.quickbite.backend.common.enums.OrderStatus;
import com.quickbite.backend.order.entity.OrderItem;
import com.quickbite.backend.restaurant.dto.TopSellingItemResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT NEW com.quickbite.backend.restaurant.dto.TopSellingItemResponse(" +
            "oi.foodItem.id, oi.foodItemName, SUM(oi.quantity), SUM(oi.totalPrice)) " +
            "FROM OrderItem oi " +
            "WHERE oi.order.restaurant.id = :restaurantId AND oi.order.status = :status " +
            "GROUP BY oi.foodItem.id, oi.foodItemName " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<TopSellingItemResponse> findTopSellingItems(Long restaurantId, OrderStatus status, Pageable pageable);
}
