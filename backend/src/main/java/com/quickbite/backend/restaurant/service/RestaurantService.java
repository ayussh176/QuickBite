package com.quickbite.backend.restaurant.service;

import com.quickbite.backend.common.enums.AccountStatus;
import com.quickbite.backend.common.enums.RestaurantStatus;
import com.quickbite.backend.common.enums.OrderStatus;
import com.quickbite.backend.coupon.repository.CouponRepository;
import com.quickbite.backend.exception.ForbiddenException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.inventory.repository.InventoryRepository;
import com.quickbite.backend.menu.repository.FoodCategoryRepository;
import com.quickbite.backend.menu.repository.FoodItemRepository;
import com.quickbite.backend.order.repository.OrderItemRepository;
import com.quickbite.backend.order.repository.OrderRepository;
import com.quickbite.backend.restaurant.dto.MerchantAnalyticsResponse;
import com.quickbite.backend.restaurant.dto.MerchantRevenueResponse;
import com.quickbite.backend.restaurant.dto.RestaurantRequest;
import com.quickbite.backend.restaurant.dto.RestaurantResponse;
import com.quickbite.backend.restaurant.entity.Restaurant;
import com.quickbite.backend.restaurant.entity.RestaurantAddress;
import com.quickbite.backend.restaurant.mapper.RestaurantMapper;
import com.quickbite.backend.restaurant.repository.RestaurantAddressRepository;
import com.quickbite.backend.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantAddressRepository addressRepository;
    private final RestaurantMapper restaurantMapper;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final FoodItemRepository foodItemRepository;
    private final FoodCategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;
    private final CouponRepository couponRepository;
    private final com.quickbite.backend.common.service.CacheService cacheService;

    private static final List<OrderStatus> COMPLETED_REVENUE_STATUSES = List.of(OrderStatus.DELIVERED);
    private static final List<OrderStatus> ACTIVE_ORDER_STATUSES = List.of(
            OrderStatus.CREATED,
            OrderStatus.CONFIRMED,
            OrderStatus.PREPARING,
            OrderStatus.READY_FOR_PICKUP,
            OrderStatus.ASSIGNED,
            OrderStatus.PICKED_UP,
            OrderStatus.OUT_FOR_DELIVERY
    );

    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "restaurants", key = "#id")
    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
        return restaurantMapper.toResponse(restaurant);
    }

    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "restaurants", key = "#slug")
    public RestaurantResponse getRestaurantBySlug(String slug) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "slug", slug));
        return restaurantMapper.toResponse(restaurant);
    }

    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "restaurants", key = "'list-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<RestaurantResponse> getAllRestaurants(Pageable pageable) {
        return restaurantRepository.findAll(pageable)
                .map(restaurantMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantByOwner(String ownerEmail) {
        Restaurant restaurant = restaurantRepository.findByUserEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant owned by " + ownerEmail, "email", ownerEmail));
        return restaurantMapper.toResponse(restaurant);
    }

    @Transactional
    public RestaurantResponse updateRestaurantProfile(String ownerEmail, RestaurantRequest request) {
        log.info("Updating profile for restaurant owned by: {}", ownerEmail);

        Restaurant restaurant = restaurantRepository.findByUserEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant owned by " + ownerEmail, "email", ownerEmail));

        // Update fields
        restaurantMapper.updateEntityFromRequest(request, restaurant);

        // Handle address update
        if (request.getAddress() != null) {
            if (restaurant.getAddress() == null) {
                RestaurantAddress newAddress = restaurantMapper.toAddressEntity(request.getAddress());
                restaurant.setAddress(newAddress);
            } else {
                restaurantMapper.updateAddressEntity(request.getAddress(), restaurant.getAddress());
            }
        }

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        log.info("Profile updated successfully for restaurant ID: {}", savedRestaurant.getId());
        cacheService.evictRestaurant(savedRestaurant.getId());

        return restaurantMapper.toResponse(savedRestaurant);
    }

    @Transactional
    public RestaurantResponse updateRestaurantStatus(Long id, RestaurantStatus status) {
        log.info("Updating status of restaurant ID: {} to {}", id, status);

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));

        restaurant.setStatus(status);
        if (status == RestaurantStatus.APPROVED) {
            restaurant.setActive(true);
            if (restaurant.getUser() != null) {
                restaurant.getUser().setAccountStatus(AccountStatus.ACTIVE);
            }
        } else if (status == RestaurantStatus.REJECTED) {
            restaurant.setActive(false);
            if (restaurant.getUser() != null) {
                restaurant.getUser().setAccountStatus(AccountStatus.INACTIVE);
            }
        } else if (status == RestaurantStatus.SUSPENDED) {
            restaurant.setActive(false);
            if (restaurant.getUser() != null) {
                restaurant.getUser().setAccountStatus(AccountStatus.SUSPENDED);
            }
        } else {
            restaurant.setActive(false);
            if (restaurant.getUser() != null) {
                restaurant.getUser().setAccountStatus(AccountStatus.PENDING_VERIFICATION);
            }
        }

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant ID: {} status updated to {}", id, status);
        cacheService.evictRestaurant(id);

        return restaurantMapper.toResponse(savedRestaurant);
    }

    @Transactional(readOnly = true)
    public MerchantRevenueResponse getMerchantRevenue(String ownerEmail) {
        Restaurant restaurant = restaurantRepository.findByUserEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant owned by " + ownerEmail, "email", ownerEmail));

        long totalOrders = orderRepository.countByRestaurantId(restaurant.getId());
        long completedOrders = orderRepository.countByRestaurantIdAndStatusIn(restaurant.getId(), COMPLETED_REVENUE_STATUSES);
        long activeOrders = orderRepository.countByRestaurantIdAndStatusIn(restaurant.getId(), ACTIVE_ORDER_STATUSES);
        BigDecimal grossRevenue = orderRepository.sumTotalAmountByRestaurantIdAndStatusIn(restaurant.getId(), COMPLETED_REVENUE_STATUSES);
        BigDecimal platformFees = grossRevenue.multiply(BigDecimal.valueOf(0.05)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netRevenue = grossRevenue.subtract(platformFees);
        BigDecimal averageOrderValue = completedOrders == 0
                ? BigDecimal.ZERO
                : grossRevenue.divide(BigDecimal.valueOf(completedOrders), 2, RoundingMode.HALF_UP);

        return MerchantRevenueResponse.builder()
                .totalOrders(totalOrders)
                .completedOrders(completedOrders)
                .activeOrders(activeOrders)
                .grossRevenue(grossRevenue)
                .platformFees(platformFees)
                .netRevenue(netRevenue)
                .averageOrderValue(averageOrderValue)
                .dailyRevenue(orderRepository.findDailyRevenueReportByRestaurantId(restaurant.getId()))
                .build();
    }

    @Transactional(readOnly = true)
    public MerchantAnalyticsResponse getMerchantAnalytics(String ownerEmail) {
        Restaurant restaurant = restaurantRepository.findByUserEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant owned by " + ownerEmail, "email", ownerEmail));

        return MerchantAnalyticsResponse.builder()
                .menuItems(foodItemRepository.countByRestaurantId(restaurant.getId()))
                .availableMenuItems(foodItemRepository.countByRestaurantIdAndAvailable(restaurant.getId(), true))
                .categories(categoryRepository.countByRestaurantIdAndActiveTrue(restaurant.getId()))
                .lowStockItems(inventoryRepository.countLowStockItemsByRestaurantId(restaurant.getId()))
                .activeCoupons(couponRepository.countByRestaurantIdAndActiveTrue(restaurant.getId()))
                .totalOrders(orderRepository.countByRestaurantId(restaurant.getId()))
                .activeOrders(orderRepository.countByRestaurantIdAndStatusIn(restaurant.getId(), ACTIVE_ORDER_STATUSES))
                .topSellingItems(orderItemRepository.findTopSellingItems(restaurant.getId(), OrderStatus.DELIVERED, PageRequest.of(0, 5)))
                .build();
    }
}
