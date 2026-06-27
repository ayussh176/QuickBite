package com.quickbite.backend.coupon.repository;

import com.quickbite.backend.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCodeIgnoreCase(String code);
    org.springframework.data.domain.Page<Coupon> findByRestaurantId(Long restaurantId, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Coupon> findByRestaurantIdIsNull(org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Coupon> findByRestaurantIdOrRestaurantIdIsNull(Long restaurantId, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Coupon c LEFT JOIN c.restaurant r WHERE (r.id = :restaurantId OR r.id IS NULL) AND c.active = true AND :now BETWEEN c.validFrom AND c.validTo")
    org.springframework.data.domain.Page<Coupon> findAvailableCoupons(Long restaurantId, java.time.LocalDateTime now, org.springframework.data.domain.Pageable pageable);
}
