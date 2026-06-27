package com.quickbite.backend.payment.controller;

import com.quickbite.backend.common.ApiResponse;
import com.quickbite.backend.payment.dto.*;
import com.quickbite.backend.payment.service.PaymentService;
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

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Controller", description = "Endpoints for initiating, verifying, and tracking UPI payments alongside merchant UPI configurations")
public class PaymentController {

    private final PaymentService paymentService;

    // ==================== Customer Payment Endpoints ====================

    @PostMapping("/initiate/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Initiate UPI payment link", description = "Generates a formatted standard UPI Payment URI matching the order's restaurant bank mapping details")
    public ResponseEntity<ApiResponse<InitiatePaymentResponse>> initiatePayment(@PathVariable Long orderId,
                                                                               Principal principal) {
        log.info("Initiating UPI payment link generation for order ID: {} by: {}", orderId, principal.getName());
        InitiatePaymentResponse response = paymentService.initiatePayment(principal.getName(), orderId);
        return ResponseEntity.ok(ApiResponse.success("UPI payment link generated successfully.", response));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify transaction status", description = "Webhook simulation route to confirm payment details and transition order and inventory stock structures")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(@Valid @RequestBody VerifyPaymentRequest request) {
        log.info("Verifying transaction ID: {} as {}", request.getTransactionId(), request.getStatus());
        PaymentResponse response = paymentService.verifyPayment(request);
        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully.", response));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get payment history", description = "Fetches a paginated history of all payments made by the authenticated customer")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getHistory(Pageable pageable, Principal principal) {
        log.info("Fetching payment history for customer: {}", principal.getName());
        Page<PaymentResponse> response = paymentService.getPaymentHistory(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Payment history fetched successfully.", response));
    }

    @GetMapping("/status/{orderId}")
    @Operation(summary = "Check payment status", description = "Finds payment details for a specific order")
    public ResponseEntity<ApiResponse<PaymentResponse>> getStatus(@PathVariable Long orderId) {
        log.info("Fetching status for order ID: {}", orderId);
        PaymentResponse response = paymentService.getPaymentStatus(orderId);
        return ResponseEntity.ok(ApiResponse.success("Payment status fetched successfully.", response));
    }

    // ==================== Merchant UPI Configuration Endpoints ====================

    @GetMapping("/upi-configs")
    @PreAuthorize("hasRole('RESTAURANT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get merchant UPI configurations", description = "Retrieves all saved UPI bank configurations for the logged-in merchant")
    public ResponseEntity<ApiResponse<List<UPIConfigurationResponse>>> getUpiConfigs(Principal principal) {
        log.info("UPI configurations requested by: {}", principal.getName());
        List<UPIConfigurationResponse> response = paymentService.getUpiConfigs(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("UPI configurations fetched successfully.", response));
    }

    @PostMapping("/upi-configs")
    @PreAuthorize("hasRole('RESTAURANT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add a merchant UPI configuration", description = "Saves a new UPI configuration and sets default status if it is the first config")
    public ResponseEntity<ApiResponse<UPIConfigurationResponse>> addUpiConfig(@Valid @RequestBody UPIConfigurationRequest request,
                                                                             Principal principal) {
        log.info("Adding UPI config requested by: {}", principal.getName());
        UPIConfigurationResponse response = paymentService.addUpiConfig(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("UPI configuration added successfully.", response));
    }

    @PutMapping("/upi-configs/{configId}")
    @PreAuthorize("hasRole('RESTAURANT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a merchant UPI configuration", description = "Modifies provider, value, and default status of an existing config")
    public ResponseEntity<ApiResponse<UPIConfigurationResponse>> updateUpiConfig(@PathVariable Long configId,
                                                                                @Valid @RequestBody UPIConfigurationRequest request,
                                                                                Principal principal) {
        log.info("Updating UPI config ID: {} by: {}", configId, principal.getName());
        UPIConfigurationResponse response = paymentService.updateUpiConfig(principal.getName(), configId, request);
        return ResponseEntity.ok(ApiResponse.success("UPI configuration updated successfully.", response));
    }

    @DeleteMapping("/upi-configs/{configId}")
    @PreAuthorize("hasRole('RESTAURANT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a merchant UPI configuration", description = "Deletes a config and elects a new default if default was removed")
    public ResponseEntity<ApiResponse<Void>> deleteUpiConfig(@PathVariable Long configId,
                                                             Principal principal) {
        log.info("Deleting UPI config ID: {} by: {}", configId, principal.getName());
        paymentService.deleteUpiConfig(principal.getName(), configId);
        return ResponseEntity.ok(ApiResponse.success("UPI configuration deleted successfully."));
    }
}
