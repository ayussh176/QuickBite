package com.quickbite.backend.menu.controller;

import com.quickbite.backend.common.ApiResponse;
import com.quickbite.backend.common.enums.FoodType;
import com.quickbite.backend.menu.dto.CategoryRequest;
import com.quickbite.backend.menu.dto.CategoryResponse;
import com.quickbite.backend.menu.dto.FoodItemRequest;
import com.quickbite.backend.menu.dto.FoodItemResponse;
import com.quickbite.backend.menu.service.MenuService;
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

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/restaurants/{restaurantId}/menu")
@RequiredArgsConstructor
@Tag(name = "Menu Controller", description = "Endpoints for managing food categories and menu items inside a restaurant")
public class MenuController {

    private final MenuService menuService;

    // ==================== Category Endpoints ====================

    @GetMapping("/categories")
    @Operation(summary = "Get all categories of a restaurant", description = "Fetches a sorted list of food categories belonging to a restaurant")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories(@PathVariable Long restaurantId) {
        log.info("Fetching categories for restaurant ID: {}", restaurantId);
        List<CategoryResponse> response = menuService.getCategoriesByRestaurant(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Categories fetched successfully.", response));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasRole('RESTAURANT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a food category", description = "Creates a new food category (e.g. Desserts) under the restaurant (only allowed for the restaurant owner)")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@PathVariable Long restaurantId,
                                                                         @Valid @RequestBody CategoryRequest request,
                                                                         Principal principal) {
        log.info("Category creation request for restaurant ID: {} from {}", restaurantId, principal.getName());
        CategoryResponse response = menuService.createCategory(principal.getName(), restaurantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully.", response));
    }

    @PutMapping("/categories/{categoryId}")
    @PreAuthorize("hasRole('RESTAURANT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a food category", description = "Updates details of an existing food category")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(@PathVariable Long restaurantId,
                                                                         @PathVariable Long categoryId,
                                                                         @Valid @RequestBody CategoryRequest request,
                                                                         Principal principal) {
        log.info("Category update request for category ID: {} from {}", categoryId, principal.getName());
        CategoryResponse response = menuService.updateCategory(principal.getName(), restaurantId, categoryId, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully.", response));
    }

    @DeleteMapping("/categories/{categoryId}")
    @PreAuthorize("hasRole('RESTAURANT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a food category", description = "Removes a food category and all items mapped under it")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long restaurantId,
                                                             @PathVariable Long categoryId,
                                                             Principal principal) {
        log.info("Category deletion request for category ID: {} from {}", categoryId, principal.getName());
        menuService.deleteCategory(principal.getName(), restaurantId, categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully."));
    }

    // ==================== Food Item Endpoints ====================

    @GetMapping("/items")
    @Operation(summary = "Get menu items with advanced filtering and search", description = "Retrieves a paginated list of active items under a restaurant, supporting search keyword matching, category filtering, food type filtering, bestseller flag, price range limits, and custom sorting")
    public ResponseEntity<ApiResponse<Page<FoodItemResponse>>> getMenuItems(@PathVariable Long restaurantId,
                                                                             @RequestParam(required = false) String search,
                                                                             @RequestParam(required = false) Long categoryId,
                                                                             @RequestParam(required = false) FoodType foodType,
                                                                             @RequestParam(required = false) BigDecimal minPrice,
                                                                             @RequestParam(required = false) BigDecimal maxPrice,
                                                                             @RequestParam(required = false) Boolean bestseller,
                                                                             Pageable pageable) {
        log.info("Searching menu items for restaurant ID: {} with query: {}", restaurantId, search);
        Page<FoodItemResponse> response = menuService.searchFoodItems(restaurantId, search, categoryId, foodType,
                minPrice, maxPrice, bestseller, pageable);
        return ResponseEntity.ok(ApiResponse.success("Menu items fetched successfully.", response));
    }

    @GetMapping("/items/{itemId}")
    @Operation(summary = "Get food item details", description = "Fetches a food item including description, price, availability and image gallery")
    public ResponseEntity<ApiResponse<FoodItemResponse>> getFoodItemDetails(@PathVariable Long restaurantId,
                                                                             @PathVariable Long itemId) {
        log.info("Fetching details for food item ID: {}", itemId);
        FoodItemResponse response = menuService.getFoodItemDetails(itemId);
        return ResponseEntity.ok(ApiResponse.success("Food item details fetched successfully.", response));
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('RESTAURANT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add a new menu item", description = "Creates a new food item under a category of the restaurant")
    public ResponseEntity<ApiResponse<FoodItemResponse>> createFoodItem(@PathVariable Long restaurantId,
                                                                         @Valid @RequestBody FoodItemRequest request,
                                                                         Principal principal) {
        log.info("Food item creation request for restaurant ID: {} from {}", restaurantId, principal.getName());
        FoodItemResponse response = menuService.createFoodItem(principal.getName(), restaurantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Food item created successfully.", response));
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('RESTAURANT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update menu item details", description = "Updates details, price, availability, and image gallery of an existing food item")
    public ResponseEntity<ApiResponse<FoodItemResponse>> updateFoodItem(@PathVariable Long restaurantId,
                                                                         @PathVariable Long itemId,
                                                                         @Valid @RequestBody FoodItemRequest request,
                                                                         Principal principal) {
        log.info("Food item update request for item ID: {} from {}", itemId, principal.getName());
        FoodItemResponse response = menuService.updateFoodItem(principal.getName(), restaurantId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Food item updated successfully.", response));
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('RESTAURANT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete menu item", description = "Removes a food item and all its image records")
    public ResponseEntity<ApiResponse<Void>> deleteFoodItem(@PathVariable Long restaurantId,
                                                             @PathVariable Long itemId,
                                                             Principal principal) {
        log.info("Food item deletion request for item ID: {} from {}", itemId, principal.getName());
        menuService.deleteFoodItem(principal.getName(), restaurantId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Food item deleted successfully."));
    }
}
