package com.quickbite.backend.admin.controller;

import com.quickbite.backend.admin.dto.*;
import com.quickbite.backend.admin.service.AdminService;
import com.quickbite.backend.common.ApiResponse;
import com.quickbite.backend.common.enums.AccountStatus;
import com.quickbite.backend.common.enums.ComplaintStatus;
import com.quickbite.backend.common.enums.OrderStatus;
import com.quickbite.backend.common.enums.RestaurantStatus;
import com.quickbite.backend.common.enums.Role;
import com.quickbite.backend.delivery.entity.DeliveryPartner;
import com.quickbite.backend.order.entity.Order;
import com.quickbite.backend.restaurant.entity.Restaurant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Admin Controller", description = "Administrative operations for dashboard stats, users, onboarding approvals, general order logs, tickets, and revenue analytics")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get admin dashboard statistics", description = "Retrieves aggregated counts for users, shops, orders, and total net revenue")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        log.info("Admin dashboard statistics requested");
        DashboardStatsResponse response = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats fetched successfully.", response));
    }

    @GetMapping("/users")
    @Operation(summary = "List users with filters", description = "Returns a paginated list of accounts filterable by Role and AccountStatus")
    public ResponseEntity<ApiResponse<Page<UserManagementResponse>>> getUsers(@RequestParam(required = false) Role role,
                                                                              @RequestParam(required = false) AccountStatus status,
                                                                              Pageable pageable) {
        log.info("Listing user accounts for admin review");
        Page<UserManagementResponse> response = adminService.listUsers(role, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Users fetched successfully.", response));
    }

    @PatchMapping("/users/{userId}/toggle-status")
    @Operation(summary = "Block or unblock a user", description = "Toggles account status between ACTIVE and BLOCKED")
    public ResponseEntity<ApiResponse<UserManagementResponse>> toggleUserStatus(@PathVariable Long userId) {
        log.info("Admin toggling account status for user ID: {}", userId);
        UserManagementResponse response = adminService.toggleUserStatus(userId);
        return ResponseEntity.ok(ApiResponse.success("User account status updated successfully.", response));
    }

    @GetMapping("/restaurants")
    @Operation(summary = "List restaurants", description = "Retrieves a paginated list of restaurants filterable by status")
    public ResponseEntity<ApiResponse<Page<Restaurant>>> getRestaurants(@RequestParam(required = false) RestaurantStatus status,
                                                                         Pageable pageable) {
        log.info("Listing restaurants for admin review");
        Page<Restaurant> response = adminService.listRestaurants(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Restaurants fetched successfully.", response));
    }

    @PatchMapping("/restaurants/{restaurantId}/approve")
    @Operation(summary = "Approve or reject a restaurant onboarding", description = "Sets restaurant status (APPROVED, REJECTED)")
    public ResponseEntity<ApiResponse<Restaurant>> approveRestaurant(@PathVariable Long restaurantId,
                                                                     @RequestParam RestaurantStatus status) {
        log.info("Admin setting restaurant ID {} approval status to {}", restaurantId, status);
        Restaurant response = adminService.toggleRestaurantApproval(restaurantId, status);
        return ResponseEntity.ok(ApiResponse.success("Restaurant approval status updated successfully.", response));
    }

    @GetMapping("/delivery-partners")
    @Operation(summary = "List delivery partners", description = "Retrieves a paginated list of riders filterable by verification status")
    public ResponseEntity<ApiResponse<Page<DeliveryPartner>>> getDeliveryPartners(@RequestParam(required = false) Boolean verified,
                                                                                   Pageable pageable) {
        log.info("Listing delivery partners for admin review");
        Page<DeliveryPartner> response = adminService.listDeliveryPartners(verified, pageable);
        return ResponseEntity.ok(ApiResponse.success("Delivery partners fetched successfully.", response));
    }

    @PatchMapping("/delivery-partners/{partnerId}/verify")
    @Operation(summary = "Verify or unverify rider", description = "Toggles driver KYC validation verified status")
    public ResponseEntity<ApiResponse<DeliveryPartner>> verifyDeliveryPartner(@PathVariable Long partnerId,
                                                                               @RequestParam boolean verified) {
        log.info("Admin setting delivery partner ID {} verified status to {}", partnerId, verified);
        DeliveryPartner response = adminService.verifyDeliveryPartner(partnerId, verified);
        return ResponseEntity.ok(ApiResponse.success("Delivery partner verification status updated.", response));
    }

    @GetMapping("/orders")
    @Operation(summary = "List all platform orders", description = "Returns a search log of all orders processed on the platform")
    public ResponseEntity<ApiResponse<Page<Order>>> getOrders(@RequestParam(required = false) OrderStatus status,
                                                              Pageable pageable) {
        log.info("Listing platform orders for admin logs");
        Page<Order> response = adminService.listAllOrders(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Orders fetched successfully.", response));
    }

    @GetMapping("/complaints")
    @Operation(summary = "List user complaints", description = "Retrieves customer support complaint tickets")
    public ResponseEntity<ApiResponse<Page<ComplaintResponse>>> getComplaints(@RequestParam(required = false) ComplaintStatus status,
                                                                              Pageable pageable) {
        log.info("Listing complaint tickets");
        Page<ComplaintResponse> response = adminService.listComplaints(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Complaints fetched successfully.", response));
    }

    @PatchMapping("/complaints/{complaintId}/resolve")
    @Operation(summary = "Resolve user complaint ticket", description = "Records resolution details and marks support ticket as RESOLVED")
    public ResponseEntity<ApiResponse<ComplaintResponse>> resolveComplaint(@PathVariable Long complaintId,
                                                                           @Valid @RequestBody ResolveComplaintRequest request) {
        log.info("Admin resolving complaint ID: {}", complaintId);
        ComplaintResponse response = adminService.resolveComplaint(complaintId, request);
        return ResponseEntity.ok(ApiResponse.success("Complaint resolved successfully.", response));
    }

    @GetMapping("/revenue/analytics")
    @Operation(summary = "Get daily revenue analytics", description = "Aggregates revenue statistics grouped daily for past deliveries")
    public ResponseEntity<ApiResponse<List<RevenueReportResponse>>> getRevenueAnalytics() {
        log.info("Revenue reports requested");
        List<RevenueReportResponse> response = adminService.getRevenueAnalytics();
        return ResponseEntity.ok(ApiResponse.success("Revenue reports fetched successfully.", response));
    }
}
