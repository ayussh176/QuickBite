package com.quickbite.backend.admin.service;

import com.quickbite.backend.admin.dto.*;
import com.quickbite.backend.admin.entity.Complaint;
import com.quickbite.backend.admin.mapper.AdminMapper;
import com.quickbite.backend.admin.repository.ComplaintRepository;
import com.quickbite.backend.auth.entity.User;
import com.quickbite.backend.auth.repository.UserRepository;
import com.quickbite.backend.common.enums.*;
import com.quickbite.backend.delivery.entity.DeliveryPartner;
import com.quickbite.backend.delivery.repository.DeliveryPartnerRepository;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.order.entity.Order;
import com.quickbite.backend.order.repository.OrderRepository;
import com.quickbite.backend.restaurant.entity.Restaurant;
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

    // ==================== Dashboard & Statistics ====================

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        log.info("Generating admin dashboard statistics");
        long totalUsers = userRepository.count();
        long totalRestaurants = restaurantRepository.count();
        long totalDeliveryPartners = deliveryPartnerRepository.count();
        long totalOrders = orderRepository.count();

        BigDecimal totalRevenue = orderRepository.sumTotalAmountByStatus(OrderStatus.DELIVERED);

        long pendingRestaurantApprovals = restaurantRepository.countByStatus(RestaurantStatus.PENDING);
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
    public Page<UserManagementResponse> listUsers(Role role, AccountStatus status, Pageable pageable) {
        if (role != null && status != null) {
            return userRepository.findByRoleAndStatus(role, status, pageable)
                    .map(adminMapper::toUserResponse);
        } else if (role != null) {
            return userRepository.findByRole(role, pageable)
                    .map(adminMapper::toUserResponse);
        } else if (status != null) {
            return userRepository.findByStatus(status, pageable)
                    .map(adminMapper::toUserResponse);
        }
        return userRepository.findAll(pageable)
                .map(adminMapper::toUserResponse);
    }

    @Transactional
    public UserManagementResponse toggleUserStatus(Long userId) {
        log.info("Toggling account status for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Admin accounts status cannot be modified.");
        }

        user.setStatus(user.getStatus() == AccountStatus.ACTIVE ? AccountStatus.BLOCKED : AccountStatus.ACTIVE);
        User saved = userRepository.save(user);

        log.info("User ID {} status updated to {}", userId, saved.getStatus());
        return adminMapper.toUserResponse(saved);
    }

    // ==================== Restaurant Approvals ====================

    @Transactional(readOnly = true)
    public Page<Restaurant> listRestaurants(RestaurantStatus status, Pageable pageable) {
        if (status != null) {
            // Find by status pageable can be added to RestaurantRepository if needed, or query JpaRepository.findAll
            return restaurantRepository.findAll((root, query, cb) -> cb.equal(root.get("status"), status), pageable);
        }
        return restaurantRepository.findAll(pageable);
    }

    @Transactional
    public Restaurant toggleRestaurantApproval(Long restaurantId, RestaurantStatus status) {
        log.info("Setting restaurant ID: {} status to {}", restaurantId, status);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        restaurant.setStatus(status);
        
        // If approved, verify the owner account
        if (status == RestaurantStatus.APPROVED) {
            User owner = restaurant.getUser();
            if (owner != null) {
                owner.setStatus(AccountStatus.ACTIVE);
                userRepository.save(owner);
            }
        }

        return restaurantRepository.save(restaurant);
    }

    // ==================== Delivery Partner Verifications ====================

    @Transactional(readOnly = true)
    public Page<DeliveryPartner> listDeliveryPartners(Boolean verified, Pageable pageable) {
        if (verified != null) {
            return deliveryPartnerRepository.findAll((root, query, cb) -> cb.equal(root.get("verified"), verified), pageable);
        }
        return deliveryPartnerRepository.findAll(pageable);
    }

    @Transactional
    public DeliveryPartner verifyDeliveryPartner(Long partnerId, boolean verified) {
        log.info("Setting delivery partner ID: {} verified status to {}", partnerId, verified);
        DeliveryPartner partner = deliveryPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPartner", "id", partnerId));

        partner.setVerified(verified);
        if (verified) {
            User user = partner.getUser();
            if (user != null) {
                user.setStatus(AccountStatus.ACTIVE);
                userRepository.save(user);
            }
        }

        return partnerRepository.save(partner);
    }

    // ==================== General Orders ====================

    @Transactional(readOnly = true)
    public Page<Order> listAllOrders(OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findByStatus(status, pageable);
        }
        return orderRepository.findAll(pageable);
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
