package com.quickbite.backend.coupon.service;

import com.quickbite.backend.coupon.dto.CouponRequest;
import com.quickbite.backend.coupon.dto.CouponResponse;
import com.quickbite.backend.coupon.entity.Coupon;
import com.quickbite.backend.coupon.mapper.CouponMapper;
import com.quickbite.backend.coupon.repository.CouponRepository;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.restaurant.entity.Restaurant;
import com.quickbite.backend.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final RestaurantRepository restaurantRepository;

    private final CouponMapper couponMapper;

    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        log.info("Creating new coupon with code: {}", request.getCode());
        if (couponRepository.findByCodeIgnoreCase(request.getCode()).isPresent()) {
            throw new BadRequestException("A coupon with code '" + request.getCode() + "' already exists.");
        }

        Coupon coupon = couponMapper.toEntity(request);

        if (request.getRestaurantId() != null) {
            Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", request.getRestaurantId()));
            coupon.setRestaurant(restaurant);
        }

        Coupon savedCoupon = couponRepository.save(coupon);
        log.info("Coupon ID: {} saved successfully", savedCoupon.getId());

        return couponMapper.toResponse(savedCoupon);
    }

    @Transactional
    public CouponResponse updateCoupon(Long id, CouponRequest request) {
        log.info("Updating coupon ID: {}", id);
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));

        couponMapper.updateEntityFromRequest(request, coupon);

        if (request.getRestaurantId() != null) {
            Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", request.getRestaurantId()));
            coupon.setRestaurant(restaurant);
        } else {
            coupon.setRestaurant(null);
        }

        Coupon savedCoupon = couponRepository.save(coupon);
        return couponMapper.toResponse(savedCoupon);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        log.info("Deleting coupon ID: {}", id);
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
        couponRepository.delete(coupon);
        log.info("Coupon ID: {} deleted successfully", id);
    }

    @Transactional(readOnly = true)
    public CouponResponse getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
        return couponMapper.toResponse(coupon);
    }

    @Transactional(readOnly = true)
    public Page<CouponResponse> getCoupons(Long restaurantId, Boolean isGlobal, Pageable pageable) {
        if (isGlobal != null && isGlobal) {
            return couponRepository.findByRestaurantIdIsNull(pageable)
                    .map(couponMapper::toResponse);
        } else if (restaurantId != null) {
            return couponRepository.findByRestaurantId(restaurantId, pageable)
                    .map(couponMapper::toResponse);
        }
        return couponRepository.findAll(pageable)
                .map(couponMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CouponResponse> getAvailableCouponsForRestaurant(Long restaurantId, Pageable pageable) {
        log.info("Fetching available coupons applicable to restaurant ID: {}", restaurantId);
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findAvailableCoupons(restaurantId, now, pageable)
                .map(couponMapper::toResponse);
    }
}
