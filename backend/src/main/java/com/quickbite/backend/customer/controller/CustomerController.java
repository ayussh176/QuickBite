package com.quickbite.backend.customer.controller;

import com.quickbite.backend.common.ApiResponse;
import com.quickbite.backend.customer.dto.CustomerAddressDto;
import com.quickbite.backend.customer.dto.CustomerRequest;
import com.quickbite.backend.customer.dto.CustomerResponse;
import com.quickbite.backend.customer.service.CustomerService;
import com.quickbite.backend.menu.dto.FoodItemResponse;
import com.quickbite.backend.order.dto.OrderResponse;
import com.quickbite.backend.restaurant.dto.RestaurantResponse;
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
@RequestMapping("/v1/customers")
@PreAuthorize("hasRole('CUSTOMER')")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Customer Controller", description = "Endpoints for managing customer profile, addresses, saved restaurants, wishlist items, and order history")
public class CustomerController {

    private final CustomerService customerService;

    // ==================== Profile Endpoints ====================

    @GetMapping("/profile")
    @Operation(summary = "Get customer profile", description = "Retrieves profile configurations and verification status for the logged-in customer")
    public ResponseEntity<ApiResponse<CustomerResponse>> getProfile(Principal principal) {
        log.info("Profile retrieval requested by: {}", principal.getName());
        CustomerResponse response = customerService.getProfile(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully.", response));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update customer profile", description = "Modifies first name, last name, and date of birth of the logged-in customer")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateProfile(@Valid @RequestBody CustomerRequest request,
                                                                       Principal principal) {
        log.info("Profile update requested by: {}", principal.getName());
        CustomerResponse response = customerService.updateProfile(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully.", response));
    }

    // ==================== Address Endpoints ====================

    @GetMapping("/addresses")
    @Operation(summary = "List all customer addresses", description = "Retrieves all saved addresses for the authenticated customer")
    public ResponseEntity<ApiResponse<List<CustomerAddressDto>>> getAddresses(Principal principal) {
        log.info("Addresses requested by: {}", principal.getName());
        List<CustomerAddressDto> response = customerService.getAddresses(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Addresses fetched successfully.", response));
    }

    @PostMapping("/addresses")
    @Operation(summary = "Add an address", description = "Saves a new address under the customer's account and elects default if it's the first address")
    public ResponseEntity<ApiResponse<CustomerAddressDto>> addAddress(@Valid @RequestBody CustomerAddressDto request,
                                                                      Principal principal) {
        log.info("Adding new address for customer: {}", principal.getName());
        CustomerAddressDto response = customerService.addAddress(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address added successfully.", response));
    }

    @PutMapping("/addresses/{addressId}")
    @Operation(summary = "Update an address", description = "Modifies details, type, coordinates, and default status of an existing address")
    public ResponseEntity<ApiResponse<CustomerAddressDto>> updateAddress(@PathVariable Long addressId,
                                                                         @Valid @RequestBody CustomerAddressDto request,
                                                                         Principal principal) {
        log.info("Updating address ID: {} for customer: {}", addressId, principal.getName());
        CustomerAddressDto response = customerService.updateAddress(principal.getName(), addressId, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully.", response));
    }

    @DeleteMapping("/addresses/{addressId}")
    @Operation(summary = "Delete an address", description = "Removes a saved address and elects a new default if the deleted address was set as default")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long addressId,
                                                           Principal principal) {
        log.info("Deleting address ID: {} for customer: {}", addressId, principal.getName());
        customerService.deleteAddress(principal.getName(), addressId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully."));
    }

    // ==================== Saved Restaurants Endpoints ====================

    @PostMapping("/saved-restaurants/{restaurantId}")
    @Operation(summary = "Toggle saved restaurant status", description = "Adds or removes a restaurant from the customer's saved/favorite list")
    public ResponseEntity<ApiResponse<Void>> toggleSavedRestaurant(@PathVariable Long restaurantId,
                                                                   Principal principal) {
        log.info("Toggling saved restaurant ID: {} for customer: {}", restaurantId, principal.getName());
        customerService.toggleSavedRestaurant(principal.getName(), restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Saved restaurant status updated successfully."));
    }

    @GetMapping("/saved-restaurants")
    @Operation(summary = "Get all saved restaurants", description = "Retrieves a list of all favorite restaurants saved by the customer")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getSavedRestaurants(Principal principal) {
        log.info("Saved restaurants list requested by: {}", principal.getName());
        List<RestaurantResponse> response = customerService.getSavedRestaurants(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Saved restaurants fetched successfully.", response));
    }

    // ==================== Wishlist Endpoints ====================

    @PostMapping("/wishlist/{foodItemId}")
    @Operation(summary = "Toggle wishlisted food item", description = "Adds or removes a food item from the customer's wishlist")
    public ResponseEntity<ApiResponse<Void>> toggleWishlistItem(@PathVariable Long foodItemId,
                                                                Principal principal) {
        log.info("Toggling wishlisted food item ID: {} for customer: {}", foodItemId, principal.getName());
        customerService.toggleWishlistItem(principal.getName(), foodItemId);
        return ResponseEntity.ok(ApiResponse.success("Wishlist item status updated successfully."));
    }

    @GetMapping("/wishlist")
    @Operation(summary = "Get all wishlisted food items", description = "Retrieves a list of all items saved in the customer's wishlist")
    public ResponseEntity<ApiResponse<List<FoodItemResponse>>> getWishlist(Principal principal) {
        log.info("Wishlist requested by: {}", principal.getName());
        List<FoodItemResponse> response = customerService.getWishlist(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Wishlist fetched successfully.", response));
    }

    // ==================== Order History Endpoints ====================

    @GetMapping("/orders")
    @Operation(summary = "Get order history", description = "Retrieves a paginated list of all past orders placed by the customer, sorted with newest orders first")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrders(Pageable pageable, Principal principal) {
        log.info("Order history requested by customer: {}", principal.getName());
        Page<OrderResponse> response = customerService.getOrderHistory(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Order history fetched successfully.", response));
    }
}
