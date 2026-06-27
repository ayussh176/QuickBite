package com.quickbite.backend.coupon.controller;

import com.quickbite.backend.common.ApiResponse;
import com.quickbite.backend.coupon.dto.CouponRequest;
import com.quickbite.backend.coupon.dto.CouponResponse;
import com.quickbite.backend.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupon Controller", description = "Endpoints for creating, managing, and retrieving promo coupons")
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a coupon (Admin/Merchant)", description = "Creates a new global or restaurant-specific promo coupon code")
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(@Valid @RequestBody CouponRequest request) {
        log.info("Creating new coupon code: {}", request.getCode());
        CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created successfully.", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a coupon", description = "Modifies value, type, expiry dates, and scope of an existing coupon")
    public ResponseEntity<ApiResponse<CouponResponse>> updateCoupon(@PathVariable Long id,
                                                                    @Valid @RequestBody CouponRequest request) {
        log.info("Updating coupon ID: {}", id);
        CouponResponse response = couponService.updateCoupon(id, request);
        return ResponseEntity.ok(ApiResponse.success("Coupon updated successfully.", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a coupon", description = "Deletes a coupon code completely from the system")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long id) {
        log.info("Deleting coupon ID: {}", id);
        couponService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.success("Coupon deleted successfully."));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get coupon details", description = "Retrieves configurations for a specific coupon code by its unique ID")
    public ResponseEntity<ApiResponse<CouponResponse>> getCouponById(@PathVariable Long id) {
        log.info("Fetching details for coupon ID: {}", id);
        CouponResponse response = couponService.getCouponById(id);
        return ResponseEntity.ok(ApiResponse.success("Coupon details fetched successfully.", response));
    }

    @GetMapping
    @Operation(summary = "List all coupons", description = "Retrieves a paginated list of coupons, with optional filters for restaurant ID or global-only status")
    public ResponseEntity<ApiResponse<Page<CouponResponse>>> getCoupons(@RequestParam(required = false) Long restaurantId,
                                                                        @RequestParam(required = false) Boolean isGlobal,
                                                                        Pageable pageable) {
        log.info("Listing coupons with filter restaurantId: {}, isGlobal: {}", restaurantId, isGlobal);
        Page<CouponResponse> response = couponService.getCoupons(restaurantId, isGlobal, pageable);
        return ResponseEntity.ok(ApiResponse.success("Coupons fetched successfully.", response));
    }

    @GetMapping("/restaurant/{restaurantId}/available")
    @Operation(summary = "List available checkout coupons", description = "Fetches a list of active, unexpired coupons applicable to the customer's cart under a specific restaurant")
    public ResponseEntity<ApiResponse<Page<CouponResponse>>> getAvailableCoupons(@PathVariable Long restaurantId,
                                                                                 Pageable pageable) {
        log.info("Listing available coupons for checkout at restaurant ID: {}", restaurantId);
        Page<CouponResponse> response = couponService.getAvailableCouponsForRestaurant(restaurantId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Available coupons fetched successfully.", response));
    }
}
