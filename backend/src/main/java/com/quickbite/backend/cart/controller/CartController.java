package com.quickbite.backend.cart.controller;

import com.quickbite.backend.cart.dto.AddCartItemRequest;
import com.quickbite.backend.cart.dto.ApplyCouponRequest;
import com.quickbite.backend.cart.dto.CartResponse;
import com.quickbite.backend.cart.dto.UpdateCartItemRequest;
import com.quickbite.backend.cart.service.CartService;
import com.quickbite.backend.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/v1/carts")
@PreAuthorize("hasRole('CUSTOMER')")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Cart Controller", description = "Endpoints for managing shopping carts, calculating order totals, taxes, delivery fees, and applying coupons")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get current cart", description = "Retrieves the logged-in customer's cart with subtotal, tax, delivery charges, discounts, and total calculations")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Principal principal) {
        log.info("Cart lookup requested by: {}", principal.getName());
        CartResponse response = cartService.getCart(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Cart fetched successfully.", response));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Adds a food item to the cart. Rejects requests adding items from multiple restaurants in a single cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@Valid @RequestBody AddCartItemRequest request,
                                                             Principal principal) {
        log.info("Adding item ID {} to cart of: {}", request.getFoodItemId(), principal.getName());
        CartResponse response = cartService.addItem(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart successfully.", response));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update item quantity", description = "Modifies quantity for a specific cart item record. Removes the item from cart if quantity is set to 0")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(@PathVariable Long itemId,
                                                                    @Valid @RequestBody UpdateCartItemRequest request,
                                                                    Principal principal) {
        log.info("Updating quantity of cart item ID: {} to {} by: {}", itemId, request.getQuantity(), principal.getName());
        CartResponse response = cartService.updateQuantity(principal.getName(), itemId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Cart item quantity updated successfully.", response));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Removes a specific cart item from the shopping cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(@PathVariable Long itemId,
                                                                Principal principal) {
        log.info("Removing cart item ID: {} by: {}", itemId, principal.getName());
        CartResponse response = cartService.removeItem(principal.getName(), itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully.", response));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear cart", description = "Deletes all items and clears the applied coupon in the shopping cart")
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(Principal principal) {
        log.info("Clearing cart requested by: {}", principal.getName());
        CartResponse response = cartService.clearCart(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully.", response));
    }

    @PostMapping("/coupon")
    @Operation(summary = "Apply coupon to cart", description = "Applies a valid promo coupon code to calculate discount values")
    public ResponseEntity<ApiResponse<CartResponse>> applyCoupon(@Valid @RequestBody ApplyCouponRequest request,
                                                                 Principal principal) {
        log.info("Applying coupon code: {} requested by: {}", request.getCouponCode(), principal.getName());
        CartResponse response = cartService.applyCoupon(principal.getName(), request.getCouponCode());
        return ResponseEntity.ok(ApiResponse.success("Coupon applied successfully.", response));
    }

    @DeleteMapping("/coupon")
    @Operation(summary = "Remove coupon from cart", description = "Removes the applied promo coupon code and resets calculations")
    public ResponseEntity<ApiResponse<CartResponse>> removeCoupon(Principal principal) {
        log.info("Removing coupon requested by: {}", principal.getName());
        CartResponse response = cartService.removeCoupon(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Coupon removed successfully.", response));
    }
}
