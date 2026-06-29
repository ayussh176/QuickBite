package com.quickbite.backend.order.service;

import com.quickbite.backend.cart.entity.Cart;
import com.quickbite.backend.cart.entity.CartItem;
import com.quickbite.backend.cart.repository.CartItemRepository;
import com.quickbite.backend.cart.repository.CartRepository;
import com.quickbite.backend.common.enums.*;
import com.quickbite.backend.coupon.entity.Coupon;
import com.quickbite.backend.coupon.repository.CouponRepository;
import com.quickbite.backend.customer.entity.Customer;
import com.quickbite.backend.customer.entity.CustomerAddress;
import com.quickbite.backend.customer.repository.CustomerAddressRepository;
import com.quickbite.backend.customer.repository.CustomerRepository;
import com.quickbite.backend.delivery.entity.DeliveryPartner;
import com.quickbite.backend.delivery.repository.DeliveryPartnerRepository;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ConflictException;
import com.quickbite.backend.exception.ForbiddenException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.inventory.entity.Inventory;
import com.quickbite.backend.inventory.repository.InventoryRepository;
import com.quickbite.backend.order.dto.InvoiceResponse;
import com.quickbite.backend.order.dto.OrderItemResponse;
import com.quickbite.backend.order.dto.OrderResponse;
import com.quickbite.backend.order.dto.PlaceOrderRequest;
import com.quickbite.backend.order.dto.OrderStatusUpdateRequest;
import com.quickbite.backend.order.entity.Order;
import com.quickbite.backend.order.entity.OrderItem;
import com.quickbite.backend.order.mapper.OrderMapper;
import com.quickbite.backend.order.repository.OrderItemRepository;
import com.quickbite.backend.order.repository.OrderRepository;
import com.quickbite.backend.payment.entity.Payment;
import com.quickbite.backend.payment.repository.PaymentRepository;
import com.quickbite.backend.restaurant.entity.Restaurant;
import com.quickbite.backend.restaurant.repository.RestaurantRepository;
import com.quickbite.backend.wallet.entity.Wallet;
import com.quickbite.backend.wallet.entity.WalletTransaction;
import com.quickbite.backend.wallet.repository.WalletRepository;
import com.quickbite.backend.wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryRepository inventoryRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final PaymentRepository paymentRepository;
    private final CouponRepository couponRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final RestaurantRepository restaurantRepository;

    private final OrderMapper orderMapper;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;
    private final com.quickbite.backend.common.service.CacheService cacheService;

    private static final List<OrderStatus> RESTAURANT_QUEUE_STATUSES = List.of(
            OrderStatus.CREATED,
            OrderStatus.CONFIRMED,
            OrderStatus.PREPARING,
            OrderStatus.READY_FOR_PICKUP,
            OrderStatus.ASSIGNED,
            OrderStatus.PICKED_UP,
            OrderStatus.OUT_FOR_DELIVERY
    );

    @Transactional
    public OrderResponse placeOrder(String email, PlaceOrderRequest request) {
        log.info("Placing order for customer: {}", email);
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new BadRequestException("No cart found for this customer."));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty.");
        }

        CustomerAddress address = addressRepository.findByIdAndCustomerId(request.getDeliveryAddressId(), customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("CustomerAddress", "id", request.getDeliveryAddressId()));

        Restaurant restaurant = cart.getItems().get(0).getFoodItem().getRestaurant();

        // 1. Calculate pricing
        BigDecimal subtotal = cart.getTotalAmount();
        BigDecimal deliveryFee = restaurant.getDeliveryFee() != null ? restaurant.getDeliveryFee() : BigDecimal.ZERO;
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.05)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = BigDecimal.ZERO;

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

        BigDecimal totalAmount = subtotal.add(deliveryFee).add(tax).subtract(discount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        // 2. Validate Stock and deduct quantity
        for (CartItem cartItem : cart.getItems()) {
            Inventory inventory = inventoryRepository.findByFoodItemId(cartItem.getFoodItem().getId())
                    .orElseThrow(() -> new BadRequestException("Inventory record not found for: " + cartItem.getFoodItem().getName()));

            if (inventory.getQuantity() < cartItem.getQuantity()) {
                throw new ConflictException("Insufficient stock for item: " + cartItem.getFoodItem().getName() + ". Available: " + inventory.getQuantity());
            }

            inventory.setQuantity(inventory.getQuantity() - cartItem.getQuantity());
            inventoryRepository.save(inventory);
        }

        // 3. Process Wallet Payment if chosen
        Wallet wallet = null;
        if (request.getPaymentMethod() == PaymentMethod.WALLET) {
            wallet = walletRepository.findByUserId(customer.getUser().getId())
                    .orElseThrow(() -> new BadRequestException("Wallet not found for this customer."));

            if (wallet.getBalance().compareTo(totalAmount) < 0) {
                throw new BadRequestException("Insufficient wallet balance. Total amount: ₹" + totalAmount + ", Balance: ₹" + wallet.getBalance());
            }

            wallet.setBalance(wallet.getBalance().subtract(totalAmount));
            walletRepository.save(wallet);

            WalletTransaction transaction = WalletTransaction.builder()
                    .wallet(wallet)
                    .transactionType(TransactionType.DEBIT)
                    .amount(totalAmount)
                    .description("Payment for order")
                    .referenceId(UUID.randomUUID().toString())
                    .balanceAfter(wallet.getBalance())
                    .build();
            walletTransactionRepository.save(transaction);
        }

        // 4. Create Order
        String orderNumber = "QB" + System.currentTimeMillis();
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customer(customer)
                .restaurant(restaurant)
                .deliveryAddress(address)
                .status(OrderStatus.CREATED)
                .subtotal(subtotal)
                .deliveryFee(deliveryFee)
                .taxAmount(tax)
                .discount(discount)
                .totalAmount(totalAmount)
                .paymentMethod(request.getPaymentMethod())
                .specialInstructions(request.getSpecialInstructions())
                .placedAt(LocalDateTime.now())
                .build();

        // Map cart items to order items
        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {
            String imageUrl = null;
            if (cartItem.getFoodItem().getImages() != null && !cartItem.getFoodItem().getImages().isEmpty()) {
                imageUrl = cartItem.getFoodItem().getImages().get(0).getImageUrl();
            }
            return OrderItem.builder()
                    .order(order)
                    .foodItem(cartItem.getFoodItem())
                    .foodItemName(cartItem.getFoodItem().getName())
                    .foodItemImageUrl(imageUrl)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .totalPrice(cartItem.getTotalPrice())
                    .specialInstructions(cartItem.getSpecialInstructions())
                    .build();
        }).collect(Collectors.toList());

        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        // 5. Create Payment record
        Payment payment = Payment.builder()
                .order(savedOrder)
                .paymentMethod(request.getPaymentMethod())
                .amount(totalAmount)
                .status(request.getPaymentMethod() == PaymentMethod.WALLET ? PaymentStatus.COMPLETED : PaymentStatus.PENDING)
                .transactionId(request.getPaymentMethod() == PaymentMethod.WALLET ? UUID.randomUUID().toString() : null)
                .paidAt(request.getPaymentMethod() == PaymentMethod.WALLET ? LocalDateTime.now() : null)
                .build();
        paymentRepository.save(payment);

        // 6. Clear Cart
        cartItemRepository.deleteByCartId(cart.getId());
        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setAppliedCouponCode(null);
        cartRepository.save(cart);

        log.info("Order placed successfully. Order Number: {}", savedOrder.getOrderNumber());
        cacheService.evictWallet(email);
        publishOrderUpdate(savedOrder);
        paymentRepository.findByOrderId(savedOrder.getId()).ifPresent(p -> {
            publishPaymentUpdate(p, savedOrder);
        });

        return orderMapper.toResponse(savedOrder);
    }

    @Transactional
    public OrderResponse cancelOrder(String email, Long orderId, String reason) {
        log.info("Cancelling order ID: {} by user: {}", orderId, email);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Security check
        if (!order.getCustomer().getUser().getEmail().equals(email)) {
            throw new ForbiddenException("You do not have permission to cancel this order.");
        }

        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Cannot cancel order in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(reason);

        // Restock items
        for (OrderItem item : order.getItems()) {
            if (item.getFoodItem() != null) {
                inventoryRepository.findByFoodItemId(item.getFoodItem().getId()).ifPresent(inv -> {
                    inv.setQuantity(inv.getQuantity() + item.getQuantity());
                    inventoryRepository.save(inv);
                });
            }
        }

        // Refund wallet payments
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment for order ID " + orderId, "orderId", orderId));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            if (order.getPaymentMethod() == PaymentMethod.WALLET) {
                Wallet wallet = walletRepository.findByUserId(order.getCustomer().getUser().getId())
                        .orElseThrow(() -> new BadRequestException("Wallet not found."));

                wallet.setBalance(wallet.getBalance().add(order.getTotalAmount()));
                walletRepository.save(wallet);

                WalletTransaction transaction = WalletTransaction.builder()
                        .wallet(wallet)
                        .transactionType(TransactionType.CREDIT)
                        .amount(order.getTotalAmount())
                        .description("Refund for cancelled order " + order.getOrderNumber())
                        .referenceId(UUID.randomUUID().toString())
                        .balanceAfter(wallet.getBalance())
                        .build();
                walletTransactionRepository.save(transaction);
            }
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order ID: {} cancelled and refunded successfully", orderId);
        cacheService.evictWallet(email);
        publishOrderUpdate(savedOrder);
        paymentRepository.findByOrderId(savedOrder.getId()).ifPresent(p -> {
            publishPaymentUpdate(p, savedOrder);
        });

        return orderMapper.toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse trackOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(Long orderId) {
        return trackOrder(orderId);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getCustomerOrderHistory(String email, Pageable pageable) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
        return orderRepository.findByCustomerIdOrderByPlacedAtDesc(customer.getId(), pageable)
                .map(orderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getRestaurantOrderHistory(String email, Pageable pageable) {
        Restaurant restaurant = restaurantRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "email", email));
        return orderRepository.findByRestaurantIdOrderByPlacedAtDesc(restaurant.getId(), pageable)
                .map(orderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getRestaurantOrderQueue(String email, Pageable pageable) {
        Restaurant restaurant = restaurantRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "email", email));
        return orderRepository.findByRestaurantIdAndStatusInOrderByPlacedAtAsc(restaurant.getId(), RESTAURANT_QUEUE_STATUSES, pageable)
                .map(orderMapper::toResponse);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        log.info("Updating status of order ID: {} to {}", orderId, request.getStatus());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setStatus(request.getStatus());

        switch (request.getStatus()) {
            case CONFIRMED:
                order.setConfirmedAt(LocalDateTime.now());
                break;
            case PREPARING:
                order.setPreparedAt(LocalDateTime.now());
                break;
            case READY_FOR_PICKUP:
                // restaurant prepared food
                break;
            case ASSIGNED:
                if (request.getDeliveryPartnerId() != null) {
                    DeliveryPartner partner = deliveryPartnerRepository.findById(request.getDeliveryPartnerId())
                            .orElseThrow(() -> new ResourceNotFoundException("DeliveryPartner", "id", request.getDeliveryPartnerId()));
                    order.setDeliveryPartner(partner);
                }
                break;
            case PICKED_UP:
                order.setPickedUpAt(LocalDateTime.now());
                break;
            case DELIVERED:
                order.setDeliveredAt(LocalDateTime.now());
                // Complete payment if online/cod was pending
                paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
                    if (payment.getStatus() == PaymentStatus.PENDING) {
                        payment.setStatus(PaymentStatus.COMPLETED);
                        payment.setPaidAt(LocalDateTime.now());
                        payment.setTransactionId(UUID.randomUUID().toString());
                        paymentRepository.save(payment);
                    }
                });
                break;
            case CANCELLED:
                order.setCancelledAt(LocalDateTime.now());
                order.setCancellationReason(request.getCancellationReason() != null ? request.getCancellationReason() : "Merchant cancellation");
                break;
            default:
                break;
        }

        Order savedOrder = orderRepository.save(order);
        publishOrderUpdate(savedOrder);
        paymentRepository.findByOrderId(savedOrder.getId()).ifPresent(p -> {
            publishPaymentUpdate(p, savedOrder);
        });
        if (savedOrder.getCustomer() != null && savedOrder.getCustomer().getUser() != null) {
            cacheService.evictWallet(savedOrder.getCustomer().getUser().getEmail());
        }
        return orderMapper.toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(Long orderId) {
        log.info("Generating invoice for order ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment for order ID " + orderId, "orderId", orderId));

        String customerAddr = order.getDeliveryAddress().getAddressLine1() + ", " +
                (order.getDeliveryAddress().getAddressLine2() != null ? order.getDeliveryAddress().getAddressLine2() + ", " : "") +
                order.getDeliveryAddress().getCity() + ", " + order.getDeliveryAddress().getState() + " - " + order.getDeliveryAddress().getZipCode();

        String restAddress = "";
        if (order.getRestaurant().getAddress() != null) {
            restAddress = order.getRestaurant().getAddress().getAddressLine1() + ", " +
                    order.getRestaurant().getAddress().getCity() + ", " + order.getRestaurant().getAddress().getState() + " - " + order.getRestaurant().getAddress().getZipCode();
        }

        List<OrderItemResponse> items = order.getItems().stream()
                .map(orderMapper::toItemResponse)
                .collect(Collectors.toList());

        return InvoiceResponse.builder()
                .invoiceNumber("INV-" + order.getOrderNumber())
                .invoiceDate(order.getPlacedAt())
                .orderNumber(order.getOrderNumber())
                .restaurantName(order.getRestaurant().getName())
                .restaurantAddress(restAddress)
                .restaurantGst(order.getRestaurant().getGstNumber())
                .restaurantFssai(order.getRestaurant().getFssaiLicense())
                .customerName(order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName())
                .customerAddress(customerAddr)
                .customerEmail(order.getCustomer().getUser().getEmail())
                .customerPhone(order.getCustomer().getUser().getPhone())
                .items(items)
                .subtotal(order.getSubtotal())
                .deliveryFee(order.getDeliveryFee())
                .taxAmount(order.getTaxAmount())
                .discount(order.getDiscount())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod().name())
                .transactionId(payment.getTransactionId())
                .build();
    }

    private void publishOrderUpdate(Order order) {
        try {
            com.quickbite.backend.order.dto.OrderMessage msg = com.quickbite.backend.order.dto.OrderMessage.builder()
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .status(order.getStatus())
                    .customerEmail(order.getCustomer().getUser().getEmail())
                    .restaurantEmail(order.getRestaurant().getUser().getEmail())
                    .riderEmail(order.getDeliveryPartner() != null ? order.getDeliveryPartner().getUser().getEmail() : null)
                    .build();

            rabbitTemplate.convertAndSend(
                    com.quickbite.backend.constants.RabbitMQConstants.ORDER_EXCHANGE,
                    "order.status." + order.getId(),
                    msg
            );
            log.info("Published order update to RabbitMQ for order number: {}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to publish order update to RabbitMQ: {}", e.getMessage(), e);
        }
    }

    private void publishPaymentUpdate(Payment payment, Order order) {
        try {
            com.quickbite.backend.payment.dto.PaymentMessage msg = com.quickbite.backend.payment.dto.PaymentMessage.builder()
                    .paymentId(payment.getId())
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .status(payment.getStatus())
                    .amount(payment.getAmount())
                    .customerEmail(order.getCustomer().getUser().getEmail())
                    .build();

            rabbitTemplate.convertAndSend(
                    com.quickbite.backend.constants.RabbitMQConstants.PAYMENT_EXCHANGE,
                    "payment.status." + payment.getId(),
                    msg
            );
            log.info("Published payment update to RabbitMQ for order number: {}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to publish payment update to RabbitMQ: {}", e.getMessage(), e);
        }
    }
}
