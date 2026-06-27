package com.quickbite.backend.cart.service;

import com.quickbite.backend.cart.dto.AddCartItemRequest;
import com.quickbite.backend.cart.dto.CartItemResponse;
import com.quickbite.backend.cart.dto.CartResponse;
import com.quickbite.backend.cart.entity.Cart;
import com.quickbite.backend.cart.entity.CartItem;
import com.quickbite.backend.cart.mapper.CartMapper;
import com.quickbite.backend.cart.repository.CartItemRepository;
import com.quickbite.backend.cart.repository.CartRepository;
import com.quickbite.backend.common.enums.CouponType;
import com.quickbite.backend.coupon.entity.Coupon;
import com.quickbite.backend.coupon.repository.CouponRepository;
import com.quickbite.backend.customer.entity.Customer;
import com.quickbite.backend.customer.repository.CustomerRepository;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ConflictException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.exception.ForbiddenException;
import com.quickbite.backend.menu.entity.FoodItem;
import com.quickbite.backend.menu.repository.FoodItemRepository;
import com.quickbite.backend.restaurant.entity.Restaurant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final FoodItemRepository foodItemRepository;
    private final CouponRepository couponRepository;

    private final CartMapper cartMapper;

    @Transactional(readOnly = true)
    public CartResponse getCart(String email) {
        Customer customer = getCustomerByEmail(email);
        Cart cart = getOrCreateCart(customer);

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse addItem(String email, AddCartItemRequest request) {
        log.info("Adding item to cart for customer: {}", email);
        Customer customer = getCustomerByEmail(email);
        Cart cart = getOrCreateCart(customer);

        FoodItem foodItem = foodItemRepository.findById(request.getFoodItemId())
                .orElseThrow(() -> new ResourceNotFoundException("FoodItem", "id", request.getFoodItemId()));

        if (!foodItem.isAvailable()) {
            throw new BadRequestException("This food item is currently unavailable.");
        }

        // Single Restaurant Constraint Check
        if (!cart.getItems().isEmpty()) {
            Restaurant existingRestaurant = cart.getItems().get(0).getFoodItem().getRestaurant();
            if (!existingRestaurant.getId().equals(foodItem.getRestaurant().getId())) {
                log.warn("Conflict: Customer trying to add item from restaurant ID {} to a cart belonging to restaurant ID {}",
                        foodItem.getRestaurant().getId(), existingRestaurant.getId());
                throw new ConflictException("Your cart contains items from another restaurant. Please clear your cart first.");
            }
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndFoodItemId(cart.getId(), foodItem.getId());

        if (existingItemOpt.isPresent()) {
            CartItem cartItem = existingItemOpt.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            cartItem.updateQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        } else {
            BigDecimal unitPrice = foodItem.getDiscountedPrice() != null ? foodItem.getDiscountedPrice() : foodItem.getPrice();
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .foodItem(foodItem)
                    .quantity(request.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(unitPrice.multiply(BigDecimal.valueOf(request.getQuantity())))
                    .specialInstructions(request.getSpecialInstructions())
                    .build();
            cart.addItem(cartItem);
            cartItemRepository.save(cartItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return buildCartResponse(savedCart);
    }

    @Transactional
    public CartResponse updateQuantity(String email, Long itemId, int quantity) {
        log.info("Updating quantity for cart item ID: {} to {}", itemId, quantity);
        Customer customer = getCustomerByEmail(email);
        Cart cart = getOrCreateCart(customer);

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ForbiddenException("You do not have permission to modify this cart item.");
        }

        if (quantity <= 0) {
            cart.removeItem(cartItem);
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.updateQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return buildCartResponse(savedCart);
    }

    @Transactional
    public CartResponse removeItem(String email, Long itemId) {
        log.info("Removing cart item ID: {} from cart", itemId);
        Customer customer = getCustomerByEmail(email);
        Cart cart = getOrCreateCart(customer);

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ForbiddenException("You do not have permission to modify this cart item.");
        }

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        Cart savedCart = cartRepository.save(cart);
        return buildCartResponse(savedCart);
    }

    @Transactional
    public CartResponse clearCart(String email) {
        log.info("Clearing cart for customer: {}", email);
        Customer customer = getCustomerByEmail(email);
        Cart cart = getOrCreateCart(customer);

        cartItemRepository.deleteByCartId(cart.getId());
        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setAppliedCouponCode(null);

        Cart savedCart = cartRepository.save(cart);
        return buildCartResponse(savedCart);
    }

    @Transactional
    public CartResponse applyCoupon(String email, String couponCode) {
        log.info("Applying coupon '{}' to cart for customer: {}", couponCode, email);
        Customer customer = getCustomerByEmail(email);
        Cart cart = getOrCreateCart(customer);

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot apply coupon to an empty cart.");
        }

        Coupon coupon = couponRepository.findByCodeIgnoreCase(couponCode)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code", couponCode));

        // Validate Coupon Validity
        LocalDateTime now = LocalDateTime.now();
        if (!coupon.isActive() || now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidTo())) {
            throw new BadRequestException("This coupon has expired or is inactive.");
        }

        // Validate Usage Limit
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new BadRequestException("This coupon usage limit has been reached.");
        }

        // Validate Minimum Order Amount
        BigDecimal subtotal = cart.getTotalAmount();
        if (subtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new BadRequestException("Minimum order amount of ₹" + coupon.getMinOrderAmount() + " is required to apply this coupon.");
        }

        cart.setAppliedCouponCode(coupon.getCode());
        Cart savedCart = cartRepository.save(cart);

        log.info("Coupon applied successfully to cart ID: {}", savedCart.getId());
        return buildCartResponse(savedCart);
    }

    @Transactional
    public CartResponse removeCoupon(String email) {
        log.info("Removing coupon from cart for customer: {}", email);
        Customer customer = getCustomerByEmail(email);
        Cart cart = getOrCreateCart(customer);

        cart.setAppliedCouponCode(null);
        Cart savedCart = cartRepository.save(cart);

        return buildCartResponse(savedCart);
    }

    // ==================== Pricing Calculation Helper ====================

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(cartMapper::toItemResponse)
                .collect(Collectors.toList());

        BigDecimal subtotal = cart.getTotalAmount();
        BigDecimal deliveryFee = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        Long restaurantId = null;
        String restaurantName = null;

        if (!cart.getItems().isEmpty()) {
            Restaurant restaurant = cart.getItems().get(0).getFoodItem().getRestaurant();
            restaurantId = restaurant.getId();
            restaurantName = restaurant.getName();
            deliveryFee = restaurant.getDeliveryFee() != null ? restaurant.getDeliveryFee() : BigDecimal.ZERO;

            // 5% GST calculation
            tax = subtotal.multiply(BigDecimal.valueOf(0.05)).setScale(2, RoundingMode.HALF_UP);

            // Apply Coupon discount if code is present
            if (cart.getAppliedCouponCode() != null) {
                Optional<Coupon> couponOpt = couponRepository.findByCodeIgnoreCase(cart.getAppliedCouponCode());
                if (couponOpt.isPresent()) {
                    Coupon coupon = couponOpt.get();
                    if (subtotal.compareTo(coupon.getMinOrderAmount()) >= 0) {
                        if (coupon.getCouponType() == CouponType.PERCENTAGE) {
                            discount = subtotal.multiply(coupon.getValue().divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP));
                            if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
                                discount = coupon.getMaxDiscount();
                            }
                        } else {
                            discount = coupon.getValue();
                        }
                    }
                }
            }
        }

        total = subtotal.add(deliveryFee).add(tax).subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .subtotal(subtotal)
                .deliveryFee(deliveryFee)
                .tax(tax)
                .discount(discount.setScale(2, RoundingMode.HALF_UP))
                .totalAmount(total.setScale(2, RoundingMode.HALF_UP))
                .appliedCouponCode(cart.getAppliedCouponCode())
                .restaurantId(restaurantId)
                .restaurantName(restaurantName)
                .build();
    }

    private Customer getCustomerByEmail(String email) {
        return customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
    }

    private Cart getOrCreateCart(Customer customer) {
        return cartRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> {
                    Cart cart = Cart.builder()
                            .customer(customer)
                            .totalAmount(BigDecimal.ZERO)
                            .build();
                    return cartRepository.save(cart);
                });
    }
}
