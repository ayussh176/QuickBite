package com.quickbite.backend.admin.service;

import com.quickbite.backend.admin.dto.*;
import com.quickbite.backend.admin.entity.Complaint;
import com.quickbite.backend.admin.mapper.AdminMapper;
import com.quickbite.backend.admin.repository.ComplaintRepository;
import com.quickbite.backend.auth.entity.User;
import com.quickbite.backend.auth.repository.UserRepository;
import com.quickbite.backend.common.enums.*;
import com.quickbite.backend.delivery.dto.DeliveryPartnerResponse;
import com.quickbite.backend.delivery.entity.DeliveryPartner;
import com.quickbite.backend.delivery.mapper.DeliveryMapper;
import com.quickbite.backend.delivery.repository.DeliveryPartnerRepository;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.order.dto.OrderResponse;
import com.quickbite.backend.order.entity.Order;
import com.quickbite.backend.order.mapper.OrderMapper;
import com.quickbite.backend.order.repository.OrderRepository;
import com.quickbite.backend.restaurant.dto.RestaurantResponse;
import com.quickbite.backend.restaurant.entity.Restaurant;
import com.quickbite.backend.restaurant.mapper.RestaurantMapper;
import com.quickbite.backend.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final OrderRepository orderRepository;
    private final ComplaintRepository complaintRepository;

    private final AdminMapper adminMapper;
    private final RestaurantMapper restaurantMapper;
    private final DeliveryMapper deliveryMapper;
    private final OrderMapper orderMapper;

    // ==================== Dashboard & Statistics ====================

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        log.info("Generating admin dashboard statistics");
        long totalUsers = userRepository.count();
        long totalRestaurants = restaurantRepository.count();
        long totalDeliveryPartners = deliveryPartnerRepository.count();
        long totalOrders = orderRepository.count();

        BigDecimal totalRevenue = orderRepository.sumTotalAmountByStatus(OrderStatus.DELIVERED);

        long pendingRestaurantApprovals = restaurantRepository.countByStatus(RestaurantStatus.PENDING_APPROVAL);
        long pendingRiderVerifications = deliveryPartnerRepository.countByVerifiedFalse();
        long pendingComplaints = complaintRepository.findByStatus(ComplaintStatus.PENDING, Pageable.unpaged()).getTotalElements();

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalRestaurants(totalRestaurants)
                .totalDeliveryPartners(totalDeliveryPartners)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .pendingRestaurantApprovals(pendingRestaurantApprovals)
                .pendingRiderVerifications(pendingRiderVerifications)
                .pendingComplaints(pendingComplaints)
                .build();
    }

    // ==================== User Management ====================

    @Transactional(readOnly = true)
    public Page<UserManagementResponse> listUsers(String search, Role role, AccountStatus status, Pageable pageable) {
        org.springframework.data.jpa.domain.Specification<User> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (role != null) {
                predicates.add(cb.equal(root.get("role"), role));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("accountStatus"), status));
            }
            if (search != null && !search.isBlank()) {
                String keyword = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("email")), keyword),
                        cb.like(cb.lower(root.get("phone")), keyword)
                ));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return userRepository.findAll(spec, pageable).map(adminMapper::toUserResponse);
    }

    @Transactional
    public UserManagementResponse toggleUserStatus(Long userId) {
        log.info("Toggling account status for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Admin accounts status cannot be modified.");
        }

        user.setAccountStatus(user.getAccountStatus() == AccountStatus.ACTIVE ? AccountStatus.SUSPENDED : AccountStatus.ACTIVE);
        User saved = userRepository.save(user);

        log.info("User ID {} status updated to {}", userId, saved.getAccountStatus());
        return adminMapper.toUserResponse(saved);
    }

    @Transactional
    public UserManagementResponse updateUserRole(Long userId, Role role) {
        log.info("Updating role for user ID {} to {}", userId, role);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Admin roles cannot be modified.");
        }
        user.setRole(role);
        User saved = userRepository.save(user);
        return adminMapper.toUserResponse(saved);
    }

    @Transactional
    public UserManagementResponse updateUserStatus(Long userId, AccountStatus status) {
        log.info("Setting account status for user ID {} to {}", userId, status);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Admin accounts status cannot be modified.");
        }
        user.setAccountStatus(status);
        User saved = userRepository.save(user);
        return adminMapper.toUserResponse(saved);
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be deleted.");
        }
        
        if (user.getRole() == Role.RESTAURANT) {
            restaurantRepository.findByUserId(userId).ifPresent(restaurantRepository::delete);
        } else if (user.getRole() == Role.DELIVERY) {
            deliveryPartnerRepository.findByUserId(userId).ifPresent(deliveryPartnerRepository::delete);
        }
        
        userRepository.delete(user);
    }

    // ==================== Restaurant Approvals ====================

    @Transactional(readOnly = true)
    public Page<RestaurantResponse> listRestaurants(String search, RestaurantStatus status, Pageable pageable) {
        org.springframework.data.jpa.domain.Specification<Restaurant> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (search != null && !search.isBlank()) {
                String keyword = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), keyword),
                        cb.like(cb.lower(root.get("phone")), keyword),
                        cb.like(cb.lower(root.get("email")), keyword)
                ));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return restaurantRepository.findAll(spec, pageable).map(restaurantMapper::toResponse);
    }

    @Transactional
    public RestaurantResponse toggleRestaurantApproval(Long restaurantId, RestaurantStatus status) {
        log.info("Setting restaurant ID: {} status to {}", restaurantId, status);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        restaurant.setStatus(status);
        
        // If approved, verify the owner account
        if (status == RestaurantStatus.APPROVED) {
            restaurant.setActive(true);
            User owner = restaurant.getUser();
            if (owner != null) {
                owner.setAccountStatus(AccountStatus.ACTIVE);
                userRepository.save(owner);
            }
        } else {
            restaurant.setActive(false);
            if (restaurant.getUser() != null) {
                if (status == RestaurantStatus.REJECTED) {
                    restaurant.getUser().setAccountStatus(AccountStatus.INACTIVE);
                } else if (status == RestaurantStatus.SUSPENDED) {
                    restaurant.getUser().setAccountStatus(AccountStatus.SUSPENDED);
                } else {
                    restaurant.getUser().setAccountStatus(AccountStatus.PENDING_VERIFICATION);
                }
                userRepository.save(restaurant.getUser());
            }
        }

        Restaurant saved = restaurantRepository.save(restaurant);
        return restaurantMapper.toResponse(saved);
    }

    @Transactional
    public RestaurantResponse updateRestaurant(Long restaurantId, com.quickbite.backend.restaurant.dto.RestaurantRequest request) {
        log.info("Admin updating restaurant ID: {}", restaurantId);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));
        
        restaurantMapper.updateEntityFromRequest(request, restaurant);
        
        if (request.getAddress() != null) {
            if (restaurant.getAddress() == null) {
                com.quickbite.backend.restaurant.entity.RestaurantAddress newAddress = restaurantMapper.toAddressEntity(request.getAddress());
                restaurant.setAddress(newAddress);
            } else {
                restaurantMapper.updateAddressEntity(request.getAddress(), restaurant.getAddress());
            }
        }
        
        Restaurant saved = restaurantRepository.save(restaurant);
        return restaurantMapper.toResponse(saved);
    }

    @Transactional
    public void deleteRestaurant(Long restaurantId) {
        log.info("Deleting restaurant ID: {}", restaurantId);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));
        try {
            restaurantRepository.delete(restaurant);
        } catch (Exception e) {
            log.warn("Hard delete failed due to constraints. Soft deleting restaurant instead.");
            restaurant.setActive(false);
            restaurant.setStatus(RestaurantStatus.REJECTED);
            restaurantRepository.save(restaurant);
        }
    }

    // ==================== Delivery Partner Verifications ====================

    @Transactional(readOnly = true)
    public Page<DeliveryPartnerResponse> listDeliveryPartners(Boolean verified, Pageable pageable) {
        if (verified != null) {
            return deliveryPartnerRepository.findAll((root, query, cb) -> cb.equal(root.get("verified"), verified), pageable)
                    .map(deliveryMapper::toResponse);
        }
        return deliveryPartnerRepository.findAll(pageable)
                .map(deliveryMapper::toResponse);
    }

    @Transactional
    public DeliveryPartnerResponse verifyDeliveryPartner(Long partnerId, boolean verified) {
        log.info("Setting delivery partner ID: {} verified status to {}", partnerId, verified);
        DeliveryPartner partner = deliveryPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPartner", "id", partnerId));

        partner.setVerified(verified);
        if (verified) {
            User user = partner.getUser();
            if (user != null) {
                user.setAccountStatus(AccountStatus.ACTIVE);
                userRepository.save(user);
            }
        }

        DeliveryPartner saved = deliveryPartnerRepository.save(partner);
        return deliveryMapper.toResponse(saved);
    }

    // ==================== General Orders ====================

    @Transactional(readOnly = true)
    public Page<OrderResponse> listAllOrders(String search, OrderStatus status, String startDate, String endDate, Pageable pageable) {
        org.springframework.data.jpa.domain.Specification<Order> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            if (search != null && !search.isBlank()) {
                String keyword = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("orderNumber")), keyword),
                        cb.like(cb.lower(root.get("restaurant").get("name")), keyword),
                        cb.like(cb.lower(root.get("customer").get("firstName")), keyword),
                        cb.like(cb.lower(root.get("customer").get("lastName")), keyword),
                        cb.like(cb.lower(root.get("customer").get("user").get("email")), keyword)
                ));
            }
            
            if (startDate != null && !startDate.isBlank()) {
                try {
                    java.time.LocalDateTime start = java.time.LocalDateTime.parse(startDate);
                    predicates.add(cb.greaterThanOrEqualTo(root.get("placedAt"), start));
                } catch (Exception e) {
                    try {
                        java.time.LocalDate startD = java.time.LocalDate.parse(startDate);
                        predicates.add(cb.greaterThanOrEqualTo(root.get("placedAt"), startD.atStartOfDay()));
                    } catch (Exception ex) {}
                }
            }
            
            if (endDate != null && !endDate.isBlank()) {
                try {
                    java.time.LocalDateTime end = java.time.LocalDateTime.parse(endDate);
                    predicates.add(cb.lessThanOrEqualTo(root.get("placedAt"), end));
                } catch (Exception e) {
                    try {
                        java.time.LocalDate endD = java.time.LocalDate.parse(endDate);
                        predicates.add(cb.lessThanOrEqualTo(root.get("placedAt"), endD.atTime(23, 59, 59)));
                    } catch (Exception ex) {}
                }
            }
            
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        
        return orderRepository.findAll(spec, pageable).map(orderMapper::toResponse);
    }

    // ==================== Complaints & Support Tickets ====================

    @Transactional(readOnly = true)
    public Page<ComplaintResponse> listComplaints(ComplaintStatus status, Pageable pageable) {
        if (status != null) {
            return complaintRepository.findByStatus(status, pageable)
                    .map(adminMapper::toComplaintResponse);
        }
        return complaintRepository.findAll(pageable)
                .map(adminMapper::toComplaintResponse);
    }

    @Transactional
    public ComplaintResponse resolveComplaint(Long complaintId, ResolveComplaintRequest request) {
        log.info("Resolving support ticket ID: {}", complaintId);
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", complaintId));

        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaint.setResolutionDetails(request.getResolutionDetails());
        complaint.setResolvedAt(LocalDateTime.now());

        Complaint saved = complaintRepository.save(complaint);
        return adminMapper.toComplaintResponse(saved);
    }

    // ==================== Revenue Analytics ====================

    @Transactional(readOnly = true)
    public List<RevenueReportResponse> getRevenueAnalytics() {
        log.info("Generating revenue analytical reports");
        return orderRepository.findDailyRevenueReport();
    }
}
