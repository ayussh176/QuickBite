package com.quickbite.backend.order.controller;

import com.quickbite.backend.common.ApiResponse;
import com.quickbite.backend.order.dto.InvoiceResponse;
import com.quickbite.backend.order.dto.OrderResponse;
import com.quickbite.backend.order.dto.PlaceOrderRequest;
import com.quickbite.backend.order.dto.OrderStatusUpdateRequest;
import com.quickbite.backend.order.service.OrderService;
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

@Slf4j
@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Controller", description = "Endpoints for placing, tracking, cancelling, invoicing, and transitioning orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Place a new order", description = "Creates an order from the active cart. Deducts stock quantity and charges wallet balance if chosen as payment method")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(@Valid @RequestBody PlaceOrderRequest request,
                                                                 Principal principal) {
        log.info("Place order request received from: {}", principal.getName());
        OrderResponse response = orderService.placeOrder(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully.", response));
    }

    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cancel an order", description = "Allows customer to cancel order if it has not been prepared. Restocks items and refunds wallet payment")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long orderId,
                                                                  @RequestParam String reason,
                                                                  Principal principal) {
        log.info("Cancel order request received for order ID: {} from: {}", orderId, principal.getName());
        OrderResponse response = orderService.cancelOrder(principal.getName(), orderId, reason);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully.", response));
    }

    @GetMapping("/{orderId}/track")
    @Operation(summary = "Track order status", description = "Retrieves live status and tracking times for a specific order")
    public ResponseEntity<ApiResponse<OrderResponse>> trackOrder(@PathVariable Long orderId) {
        log.info("Tracking order ID: {}", orderId);
        OrderResponse response = orderService.trackOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order tracking fetched successfully.", response));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details", description = "Retrieves detailed breakdowns, items, customer details, and payment information for an order")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetails(@PathVariable Long orderId) {
        log.info("Fetching details for order ID: {}", orderId);
        OrderResponse response = orderService.getOrderDetails(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order details fetched successfully.", response));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get customer order history", description = "Fetches a paginated list of all past orders placed by the logged-in customer")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getHistory(Pageable pageable, Principal principal) {
        log.info("Fetching order history for customer: {}", principal.getName());
        Page<OrderResponse> response = orderService.getCustomerOrderHistory(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Order history fetched successfully.", response));
    }

    @GetMapping("/restaurant")
    @PreAuthorize("hasRole('RESTAURANT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get restaurant order history", description = "Fetches a paginated list of all orders placed at the logged-in restaurant")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getRestaurantHistory(Pageable pageable, Principal principal) {
        log.info("Fetching order history for restaurant: {}", principal.getName());
        Page<OrderResponse> response = orderService.getRestaurantOrderHistory(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Restaurant order history fetched successfully.", response));
    }

    @GetMapping("/restaurant/queue")
    @PreAuthorize("hasRole('RESTAURANT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get restaurant order queue", description = "Fetches active order workflow items for the logged-in restaurant")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getRestaurantQueue(Pageable pageable, Principal principal) {
        log.info("Fetching active order queue for restaurant: {}", principal.getName());
        Page<OrderResponse> response = orderService.getRestaurantOrderQueue(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Restaurant order queue fetched successfully.", response));
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT', 'DELIVERY')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update order status", description = "Permits merchant, driver, or admin to transition the order lifecycle and sets processing timestamps")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(@PathVariable Long orderId,
                                                                    @Valid @RequestBody OrderStatusUpdateRequest request) {
        log.info("Status update request for order ID: {} to {}", orderId, request.getStatus());
        OrderResponse response = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully.", response));
    }

    @GetMapping("/{orderId}/invoice")
    @Operation(summary = "Get order invoice", description = "Generates a structured PDF-ready invoice response mapping restaurant and customer details, items, and billing totals")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(@PathVariable Long orderId) {
        log.info("Invoice generation requested for order ID: {}", orderId);
        InvoiceResponse response = orderService.getInvoice(orderId);
        return ResponseEntity.ok(ApiResponse.success("Invoice generated successfully.", response));
    }
}
