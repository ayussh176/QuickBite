package com.quickbite.backend.delivery.controller;

import com.quickbite.backend.common.ApiResponse;
import com.quickbite.backend.common.enums.DeliveryPartnerStatus;
import com.quickbite.backend.delivery.dto.*;
import com.quickbite.backend.delivery.service.DeliveryService;
import com.quickbite.backend.order.dto.OrderResponse;
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

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/v1/delivery")
@PreAuthorize("hasRole('DELIVERY')")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Delivery Partner Controller", description = "Endpoints for delivery rider profiles, availability status, KYC registration, earnings, and order fulfillment actions")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/profile")
    @Operation(summary = "Get delivery partner profile", description = "Retrieves verification status, vehicle details, average ratings, and totals for the rider")
    public ResponseEntity<ApiResponse<DeliveryPartnerResponse>> getProfile(Principal principal) {
        log.info("Rider profile requested by: {}", principal.getName());
        DeliveryPartnerResponse response = deliveryService.getProfile(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully.", response));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update rider details", description = "Modifies first name, last name, and contact details of the rider")
    public ResponseEntity<ApiResponse<DeliveryPartnerResponse>> updateProfile(@Valid @RequestBody DeliveryPartnerRequest request,
                                                                              Principal principal) {
        log.info("Rider profile update requested by: {}", principal.getName());
        DeliveryPartnerResponse response = deliveryService.updateProfile(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully.", response));
    }

    @PutMapping("/vehicle")
    @Operation(summary = "Set vehicle details", description = "Adds or updates details of the vehicle (registration number, make, model, type)")
    public ResponseEntity<ApiResponse<DeliveryPartnerResponse>> updateVehicle(@Valid @RequestBody VehicleDto request,
                                                                              Principal principal) {
        log.info("Vehicle details update requested by rider: {}", principal.getName());
        DeliveryPartnerResponse response = deliveryService.updateVehicle(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Vehicle details updated successfully.", response));
    }

    @PostMapping("/kyc")
    @Operation(summary = "Submit KYC verification", description = "Registers driving license and Aadhar card numbers, auto-verifying rider availability")
    public ResponseEntity<ApiResponse<DeliveryPartnerResponse>> submitKyc(@Valid @RequestBody KycRequest request,
                                                                          Principal principal) {
        log.info("KYC submission requested by rider: {}", principal.getName());
        DeliveryPartnerResponse response = deliveryService.updateKyc(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("KYC details submitted and verified.", response));
    }

    @PatchMapping("/availability")
    @Operation(summary = "Set online status", description = "Updates rider availability state (AVAILABLE or OFFLINE) to receive delivery pings")
    public ResponseEntity<ApiResponse<DeliveryPartnerResponse>> setAvailability(@RequestParam DeliveryPartnerStatus status,
                                                                                Principal principal) {
        log.info("Availability status update to {} requested by: {}", status, principal.getName());
        DeliveryPartnerResponse response = deliveryService.updateAvailability(principal.getName(), status);
        return ResponseEntity.ok(ApiResponse.success("Availability status updated successfully.", response));
    }

    @PutMapping("/location")
    @Operation(summary = "Update location coordinates (HTTP)", description = "Permits periodic GPS updates via HTTP request fallback")
    public ResponseEntity<ApiResponse<Void>> updateLocation(@Valid @RequestBody GpsLocationPayload request,
                                                            Principal principal) {
        // Location update using rider profile ID
        DeliveryPartnerResponse profile = deliveryService.getProfile(principal.getName());
        deliveryService.updateLocation(profile.getId(), request.getLatitude(), request.getLongitude());
        return ResponseEntity.ok(ApiResponse.success("Coordinates updated successfully."));
    }

    @GetMapping("/pending")
    @Operation(summary = "View ready deliveries", description = "Fetches a paginated list of all orders that are currently marked READY_FOR_PICKUP and waiting for a rider")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getPendingOrders(Pageable pageable) {
        log.info("Fetching pending deliveries for rider");
        Page<OrderResponse> response = deliveryService.getPendingDeliveries(pageable);
        return ResponseEntity.ok(ApiResponse.success("Pending orders fetched successfully.", response));
    }

    @PostMapping("/orders/{orderId}/accept")
    @Operation(summary = "Accept delivery request", description = "Claims a ready delivery, marking order status as ASSIGNED")
    public ResponseEntity<ApiResponse<OrderResponse>> acceptOrder(@PathVariable Long orderId,
                                                                  Principal principal) {
        log.info("Rider {} accepting order ID: {}", principal.getName(), orderId);
        OrderResponse response = deliveryService.acceptDelivery(principal.getName(), orderId);
        return ResponseEntity.ok(ApiResponse.success("Delivery accepted successfully.", response));
    }

    @PostMapping("/orders/{orderId}/reject")
    @Operation(summary = "Reject/release delivery", description = "Releases an assigned delivery back to the public pending pool")
    public ResponseEntity<ApiResponse<OrderResponse>> rejectOrder(@PathVariable Long orderId,
                                                                  Principal principal) {
        log.info("Rider {} rejecting order ID: {}", principal.getName(), orderId);
        OrderResponse response = deliveryService.rejectDelivery(principal.getName(), orderId);
        return ResponseEntity.ok(ApiResponse.success("Delivery rejected and released back to pool.", response));
    }

    @GetMapping("/history")
    @Operation(summary = "Get delivery history", description = "Retrieves a paginated history of all past deliveries handled by this rider")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getHistory(Pageable pageable, Principal principal) {
        log.info("History request from rider: {}", principal.getName());
        Page<OrderResponse> response = deliveryService.getDeliveryHistory(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Delivery history fetched successfully.", response));
    }

    @GetMapping("/earnings")
    @Operation(summary = "View delivery earnings", description = "Calculates total earnings from delivery fees across all completed orders")
    public ResponseEntity<ApiResponse<EarningsResponse>> getEarnings(Principal principal) {
        log.info("Earnings review requested by rider: {}", principal.getName());
        EarningsResponse response = deliveryService.getEarnings(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Earnings report calculated successfully.", response));
    }
}
