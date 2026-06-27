package com.quickbite.backend.restaurant.controller;

import com.quickbite.backend.common.ApiResponse;
import com.quickbite.backend.common.enums.RestaurantStatus;
import com.quickbite.backend.restaurant.dto.RestaurantRequest;
import com.quickbite.backend.restaurant.dto.RestaurantResponse;
import com.quickbite.backend.restaurant.service.RestaurantService;
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
@RequestMapping("/v1/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurant Controller", description = "Endpoints for viewing and updating restaurant profiles")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    @Operation(summary = "Get all restaurants", description = "Fetches a paginated list of all registered restaurants on the platform")
    public ResponseEntity<ApiResponse<Page<RestaurantResponse>>> getAllRestaurants(Pageable pageable) {
        log.info("Fetching paginated restaurants");
        Page<RestaurantResponse> response = restaurantService.getAllRestaurants(pageable);
        return ResponseEntity.ok(ApiResponse.success("Restaurants fetched successfully.", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get restaurant by ID", description = "Fetches details of a specific restaurant using its ID")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getRestaurantById(@PathVariable Long id) {
        log.info("Fetching restaurant by ID: {}", id);
        RestaurantResponse response = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(ApiResponse.success("Restaurant fetched successfully.", response));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get restaurant by Slug", description = "Fetches details of a specific restaurant using its unique URL slug")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getRestaurantBySlug(@PathVariable String slug) {
        log.info("Fetching restaurant by slug: {}", slug);
        RestaurantResponse response = restaurantService.getRestaurantBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Restaurant fetched successfully.", response));
    }

    @PutMapping("/my-restaurant")
    @PreAuthorize("hasRole('RESTAURANT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update restaurant profile", description = "Updates profile configuration and coordinates of the logged-in restaurant owner")
    public ResponseEntity<ApiResponse<RestaurantResponse>> updateProfile(@Valid @RequestBody RestaurantRequest request,
                                                                         Principal principal) {
        log.info("Profile update request from restaurant owner: {}", principal.getName());
        RestaurantResponse response = restaurantService.updateRestaurantProfile(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Restaurant profile updated successfully.", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update restaurant status (Admin)", description = "Allows administrators to approve, reject, or suspend a restaurant's registration status")
    public ResponseEntity<ApiResponse<RestaurantResponse>> updateStatus(@PathVariable Long id,
                                                                         @RequestParam RestaurantStatus status) {
        log.info("Admin status update request for restaurant ID: {} to {}", id, status);
        RestaurantResponse response = restaurantService.updateRestaurantStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Restaurant status updated successfully.", response));
    }
}
