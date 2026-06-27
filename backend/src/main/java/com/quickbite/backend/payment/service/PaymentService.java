package com.quickbite.backend.payment.service;

import com.quickbite.backend.common.enums.*;
import com.quickbite.backend.customer.entity.Customer;
import com.quickbite.backend.customer.repository.CustomerRepository;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ForbiddenException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.inventory.entity.Inventory;
import com.quickbite.backend.inventory.repository.InventoryRepository;
import com.quickbite.backend.order.entity.Order;
import com.quickbite.backend.order.entity.OrderItem;
import com.quickbite.backend.order.repository.OrderRepository;
import com.quickbite.backend.payment.dto.*;
import com.quickbite.backend.payment.entity.Payment;
import com.quickbite.backend.payment.entity.UPIConfiguration;
import com.quickbite.backend.payment.mapper.PaymentMapper;
import com.quickbite.backend.payment.repository.PaymentRepository;
import com.quickbite.backend.payment.repository.UPIConfigurationRepository;
import com.quickbite.backend.restaurant.entity.Restaurant;
import com.quickbite.backend.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UPIConfigurationRepository upiRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final InventoryRepository inventoryRepository;

    private final PaymentMapper paymentMapper;

    @Transactional
    public InitiatePaymentResponse initiatePayment(String email, Long orderId) {
        log.info("Initiating payment for order ID: {}", orderId);
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new ForbiddenException("You do not have permission to pay for this order.");
        }

        if (order.getPaymentMethod() != PaymentMethod.UPI) {
            throw new BadRequestException("This payment endpoint only supports UPI transactions.");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record for order ID " + orderId, "orderId", orderId));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new BadRequestException("This order has already been paid.");
        }

        // Fetch payee UPI configurations
        Restaurant restaurant = order.getRestaurant();
        Optional<UPIConfiguration> upiConfigOpt = upiRepository.findByRestaurantIdAndIsDefaultTrue(restaurant.getId());

        String payeeUpiId = upiConfigOpt.map(UPIConfiguration::getUpiId).orElse("quickbite@paytm"); // fallback
        String payeeName = restaurant.getName();
        String transactionRefId = "TXN" + UUID.randomUUID().toString().substring(0, 18).toUpperCase();
        String note = "Order " + order.getOrderNumber();

        // Construct standard UPI Payment URI
        String upiUri = "";
        try {
            String encodedPayeeName = URLEncoder.encode(payeeName, StandardCharsets.UTF_8.toString()).replace("+", "%20");
            String encodedNote = URLEncoder.encode(note, StandardCharsets.UTF_8.toString()).replace("+", "%20");
            
            upiUri = String.format("upi://pay?pa=%s&pn=%s&am=%s&cu=INR&tn=%s&tr=%s",
                    payeeUpiId,
                    encodedPayeeName,
                    order.getTotalAmount().toString(),
                    encodedNote,
                    transactionRefId);
        } catch (Exception e) {
            log.error("Error constructing UPI URI: {}", e.getMessage());
            throw new BadRequestException("Failed to construct payment link.");
        }

        // Save reference transaction details
        payment.setTransactionId(transactionRefId);
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        log.info("UPI URI generated successfully: {}", upiUri);

        return InitiatePaymentResponse.builder()
                .upiUri(upiUri)
                .amount(order.getTotalAmount())
                .orderNumber(order.getOrderNumber())
                .transactionRefId(transactionRefId)
                .payeeUpiId(payeeUpiId)
                .payeeName(payeeName)
                .build();
    }

    @Transactional
    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {
        log.info("Verifying transaction ID: {}", request.getTransactionId());

        Payment payment = paymentRepository.findByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment with transaction ID " + request.getTransactionId(), "transactionId", request.getTransactionId()));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return paymentMapper.toResponse(payment);
        }

        Order order = payment.getOrder();

        if (request.getStatus() == PaymentStatus.COMPLETED) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPaidAt(LocalDateTime.now());

            order.setStatus(OrderStatus.CONFIRMED);
            order.setConfirmedAt(LocalDateTime.now());
            orderRepository.save(order);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(request.getFailureReason() != null ? request.getFailureReason() : "Bank transaction declined");

            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelledAt(LocalDateTime.now());
            order.setCancellationReason("Payment verification failed: " + payment.getFailureReason());
            orderRepository.save(order);

            // Restock items
            for (OrderItem item : order.getItems()) {
                if (item.getFoodItem() != null) {
                    inventoryRepository.findByFoodItemId(item.getFoodItem().getId()).ifPresent(inv -> {
                        inv.setQuantity(inv.getQuantity() + item.getQuantity());
                        inventoryRepository.save(inv);
                    });
                }
            }
        }

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Transaction ID: {} verified as {}", request.getTransactionId(), savedPayment.getStatus());

        return paymentMapper.toResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentHistory(String email, Pageable pageable) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));

        return paymentRepository.findByCustomerId(customer.getId(), pageable)
                .map(paymentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentStatus(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment for order ID " + orderId, "orderId", orderId));
        return paymentMapper.toResponse(payment);
    }

    // ==================== UPI Configuration Management ====================

    @Transactional
    public UPIConfigurationResponse addUpiConfig(String ownerEmail, UPIConfigurationRequest request) {
        log.info("Adding UPI Configuration for merchant: {}", ownerEmail);
        Restaurant restaurant = getRestaurantByOwnerEmail(ownerEmail);

        UPIConfiguration config = paymentMapper.toEntity(request);
        config.setRestaurant(restaurant);

        List<UPIConfiguration> existingConfigs = upiRepository.findByRestaurantId(restaurant.getId());
        if (existingConfigs.isEmpty()) {
            config.setDefault(true);
        } else if (config.isDefault()) {
            resetDefaultConfigs(restaurant.getId());
        }

        UPIConfiguration savedConfig = upiRepository.save(config);
        log.info("UPI Configuration ID: {} added successfully", savedConfig.getId());

        return paymentMapper.toResponse(savedConfig);
    }

    @Transactional
    public UPIConfigurationResponse updateUpiConfig(String ownerEmail, Long configId, UPIConfigurationRequest request) {
        log.info("Updating UPI Configuration ID: {} for merchant: {}", configId, ownerEmail);
        Restaurant restaurant = getRestaurantByOwnerEmail(ownerEmail);
        UPIConfiguration config = upiRepository.findByIdAndRestaurantId(configId, restaurant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("UPIConfiguration", "id", configId));

        paymentMapper.updateEntityFromRequest(request, config);

        if (request.getIsDefault() != null && request.getIsDefault()) {
            resetDefaultConfigs(restaurant.getId());
            config.setDefault(true);
        }

        UPIConfiguration savedConfig = upiRepository.save(config);
        return paymentMapper.toResponse(savedConfig);
    }

    @Transactional
    public void deleteUpiConfig(String ownerEmail, Long configId) {
        log.info("Deleting UPI Configuration ID: {} for merchant: {}", configId, ownerEmail);
        Restaurant restaurant = getRestaurantByOwnerEmail(ownerEmail);
        UPIConfiguration config = upiRepository.findByIdAndRestaurantId(configId, restaurant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("UPIConfiguration", "id", configId));

        if (config.isDefault()) {
            upiRepository.delete(config);
            List<UPIConfiguration> remaining = upiRepository.findByRestaurantId(restaurant.getId());
            if (!remaining.isEmpty()) {
                UPIConfiguration newDefault = remaining.get(0);
                newDefault.setDefault(true);
                upiRepository.save(newDefault);
            }
        } else {
            upiRepository.delete(config);
        }
        log.info("UPI Configuration ID: {} deleted successfully", configId);
    }

    @Transactional(readOnly = true)
    public List<UPIConfigurationResponse> getUpiConfigs(String ownerEmail) {
        Restaurant restaurant = getRestaurantByOwnerEmail(ownerEmail);
        return upiRepository.findByRestaurantId(restaurant.getId())
                .stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== Helper Methods ====================

    private Restaurant getRestaurantByOwnerEmail(String ownerEmail) {
        return restaurantRepository.findByUserEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant owned by " + ownerEmail, "email", ownerEmail));
    }

    private void resetDefaultConfigs(Long restaurantId) {
        List<UPIConfiguration> configs = upiRepository.findByRestaurantId(restaurantId);
        configs.forEach(c -> {
            if (c.isDefault()) {
                c.setDefault(false);
                upiRepository.save(c);
            }
        });
    }
}
