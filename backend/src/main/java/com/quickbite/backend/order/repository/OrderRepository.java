package com.quickbite.backend.order.repository;

import com.quickbite.backend.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCustomerIdOrderByPlacedAtDesc(Long customerId, Pageable pageable);
    Page<Order> findByDeliveryPartnerIdOrderByPlacedAtDesc(Long deliveryPartnerId, Pageable pageable);
    java.util.List<Order> findByDeliveryPartnerIdAndStatusIn(Long deliveryPartnerId, java.util.List<com.quickbite.backend.common.enums.OrderStatus> statuses);
    Page<Order> findByStatus(com.quickbite.backend.common.enums.OrderStatus status, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status")
    java.math.BigDecimal sumTotalAmountByStatus(com.quickbite.backend.common.enums.OrderStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT NEW com.quickbite.backend.admin.dto.RevenueReportResponse(" +
            "CAST(o.placedAt AS localdate), " +
            "SUM(o.subtotal), " +
            "SUM(o.deliveryFee), " +
            "SUM(o.taxAmount), " +
            "SUM(o.discount), " +
            "SUM(o.totalAmount)) " +
            "FROM Order o WHERE o.status = 'DELIVERED' " +
            "GROUP BY CAST(o.placedAt AS localdate) " +
            "ORDER BY CAST(o.placedAt AS localdate) DESC")
    java.util.List<com.quickbite.backend.admin.dto.RevenueReportResponse> findDailyRevenueReport();
}
