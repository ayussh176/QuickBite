package com.quickbite.backend.coupon.service;

import com.quickbite.backend.auth.entity.User;
import com.quickbite.backend.auth.repository.UserRepository;
import com.quickbite.backend.common.enums.Role;
import com.quickbite.backend.coupon.dto.CouponRequest;
import com.quickbite.backend.coupon.dto.CouponResponse;
import com.quickbite.backend.coupon.entity.Coupon;
import com.quickbite.backend.coupon.mapper.CouponMapper;
import com.quickbite.backend.coupon.repository.CouponRepository;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ForbiddenException;
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
    private final UserRepository userRepository;

    private final CouponMapper couponMapper;
    private final com.quickbite.backend.common.service.CacheService cacheService;

    @Transactional
    public CouponResponse createCoupon(String actorEmail, CouponRequest request) {
        log.info("Creating new coupon with code: {}", request.getCode());
        if (couponRepository.findByCodeIgnoreCase(request.getCode()).isPresent()) {
            throw new BadRequestException("A coupon with code '" + request.getCode() + "' already exists.");
        }

        Coupon coupon = couponMapper.toEntity(request);
        Restaurant scopedRestaurant = resolveScopedRestaurant(actorEmail, request.getRestaurantId());

        coupon.setRestaurant(scopedRestaurant);

        Coupon savedCoupon = couponRepository.save(coupon);
        log.info("Coupon ID: {} saved successfully", savedCoupon.getId());
        cacheService.evictCoupons(request.getRestaurantId());

        return couponMapper.toResponse(savedCoupon);
    }

    @Transactional
    public CouponResponse updateCoupon(String actorEmail, Long id, CouponRequest request) {
        log.info("Updating coupon ID: {}", id);
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));

        validateCouponAccess(actorEmail, coupon);
        couponMapper.updateEntityFromRequest(request, coupon);
        Restaurant scopedRestaurant = resolveScopedRestaurant(actorEmail, request.getRestaurantId());
        coupon.setRestaurant(scopedRestaurant);

        Coupon savedCoupon = couponRepository.save(coupon);
        cacheService.evictCoupons(request.getRestaurantId());
        return couponMapper.toResponse(savedCoupon);
    }

    @Transactional
    public void deleteCoupon(String actorEmail, Long id) {
        log.info("Deleting coupon ID: {}", id);
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
        validateCouponAccess(actorEmail, coupon);
        couponRepository.delete(coupon);
        log.info("Coupon ID: {} deleted successfully", id);
        if (coupon.getRestaurant() != null) {
            cacheService.evictCoupons(coupon.getRestaurant().getId());
        } else {
            cacheService.evictCoupons(null);
        }
    }

    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "coupons", key = "#id")
    public CouponResponse getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
        return couponMapper.toResponse(coupon);
    }

    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "coupons", key = "(#restaurantId != null ? #restaurantId.toString() : 'all') + '-' + (#isGlobal != null ? #isGlobal.toString() : 'all') + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
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

    private Restaurant resolveScopedRestaurant(String actorEmail, Long requestedRestaurantId) {
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", actorEmail));

        if (actor.getRole() == Role.ADMIN) {
            if (requestedRestaurantId == null) {
                return null;
            }
            return restaurantRepository.findById(requestedRestaurantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", requestedRestaurantId));
        }

        if (actor.getRole() == Role.RESTAURANT) {
            Restaurant restaurant = restaurantRepository.findByUserEmail(actorEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurant owned by " + actorEmail, "email", actorEmail));
            if (requestedRestaurantId != null && !restaurant.getId().equals(requestedRestaurantId)) {
                throw new ForbiddenException("You cannot manage coupons for another restaurant.");
            }
            return restaurant;
        }

        throw new ForbiddenException("You do not have permission to manage coupons.");
    }

    private void validateCouponAccess(String actorEmail, Coupon coupon) {
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", actorEmail));
        if (actor.getRole() == Role.ADMIN) {
            return;
        }
        if (actor.getRole() != Role.RESTAURANT || coupon.getRestaurant() == null) {
            throw new ForbiddenException("You do not have permission to manage this coupon.");
        }
        Restaurant restaurant = restaurantRepository.findByUserEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant owned by " + actorEmail, "email", actorEmail));
        if (!restaurant.getId().equals(coupon.getRestaurant().getId())) {
            throw new ForbiddenException("You cannot manage coupons for another restaurant.");
        }
    }
}
