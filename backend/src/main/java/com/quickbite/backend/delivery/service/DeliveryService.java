package com.quickbite.backend.delivery.service;

import com.quickbite.backend.common.enums.DeliveryPartnerStatus;
import com.quickbite.backend.common.enums.OrderStatus;
import com.quickbite.backend.delivery.dto.*;
import com.quickbite.backend.delivery.entity.DeliveryPartner;
import com.quickbite.backend.delivery.entity.Vehicle;
import com.quickbite.backend.delivery.mapper.DeliveryMapper;
import com.quickbite.backend.delivery.repository.DeliveryPartnerRepository;
import com.quickbite.backend.delivery.repository.VehicleRepository;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ForbiddenException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.order.dto.OrderResponse;
import com.quickbite.backend.order.entity.Order;
import com.quickbite.backend.order.mapper.OrderMapper;
import com.quickbite.backend.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryPartnerRepository partnerRepository;
    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;

    private final DeliveryMapper deliveryMapper;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public DeliveryPartnerResponse getProfile(String email) {
        DeliveryPartner partner = getPartnerByEmail(email);
        return deliveryMapper.toResponse(partner);
    }

    @Transactional
    public DeliveryPartnerResponse updateProfile(String email, DeliveryPartnerRequest request) {
        log.info("Updating delivery partner profile for: {}", email);
        DeliveryPartner partner = getPartnerByEmail(email);

        deliveryMapper.updateEntityFromRequest(request, partner);
        DeliveryPartner savedPartner = partnerRepository.save(partner);

        return deliveryMapper.toResponse(savedPartner);
    }

    @Transactional
    public DeliveryPartnerResponse updateVehicle(String email, VehicleDto vehicleDto) {
        log.info("Updating vehicle details for delivery partner: {}", email);
        DeliveryPartner partner = getPartnerByEmail(email);

        Vehicle vehicle = partner.getVehicle();
        if (vehicle == null) {
            vehicle = deliveryMapper.toVehicleEntity(vehicleDto);
            partner.setVehicle(vehicle);
        } else {
            deliveryMapper.updateVehicleEntityFromDto(vehicleDto, vehicle);
        }

        vehicleRepository.save(vehicle);
        DeliveryPartner savedPartner = partnerRepository.save(partner);

        return deliveryMapper.toResponse(savedPartner);
    }

    @Transactional
    public DeliveryPartnerResponse updateKyc(String email, KycRequest request) {
        log.info("Updating KYC details for: {}", email);
        DeliveryPartner partner = getPartnerByEmail(email);

        partner.setDrivingLicenseNumber(request.getDrivingLicenseNumber());
        partner.setAadharNumber(request.getAadharNumber());
        partner.setVerified(true); // Auto-approve KYC for simplicity in this stage

        DeliveryPartner savedPartner = partnerRepository.save(partner);
        return deliveryMapper.toResponse(savedPartner);
    }

    @Transactional
    public DeliveryPartnerResponse updateAvailability(String email, DeliveryPartnerStatus status) {
        log.info("Updating availability status to {} for: {}", status, email);
        DeliveryPartner partner = getPartnerByEmail(email);

        partner.setStatus(status);
        DeliveryPartner savedPartner = partnerRepository.save(partner);

        return deliveryMapper.toResponse(savedPartner);
    }

    @Transactional
    public void updateLocation(Long partnerId, Double latitude, Double longitude) {
        log.debug("Updating coordinates to ({}, {}) for partner ID: {}", latitude, longitude, partnerId);
        DeliveryPartner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPartner", "id", partnerId));

        partner.setCurrentLatitude(latitude);
        partner.setCurrentLongitude(longitude);
        partnerRepository.save(partner);
    }

    @Transactional
    public OrderResponse acceptDelivery(String email, Long orderId) {
        log.info("Delivery partner {} attempting to accept order ID: {}", email, orderId);
        DeliveryPartner partner = getPartnerByEmail(email);

        if (!partner.isVerified()) {
            throw new BadRequestException("Your KYC must be verified before you can accept deliveries.");
        }

        if (partner.getStatus() == DeliveryPartnerStatus.OFFLINE) {
            throw new BadRequestException("You must go online to accept deliveries.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            throw new BadRequestException("This order is not ready for pickup. Current status: " + order.getStatus());
        }

        if (order.getDeliveryPartner() != null) {
            throw new BadRequestException("This order has already been assigned to another rider.");
        }

        order.setDeliveryPartner(partner);
        order.setStatus(OrderStatus.ASSIGNED);
        Order savedOrder = orderRepository.save(order);

        partner.setStatus(DeliveryPartnerStatus.ON_DELIVERY);
        partnerRepository.save(partner);

        log.info("Order ID: {} assigned to rider: {}", orderId, partner.getFirstName());
        return orderMapper.toResponse(savedOrder);
    }

    @Transactional
    public OrderResponse rejectDelivery(String email, Long orderId) {
        log.info("Rider {} rejecting order ID: {}", email, orderId);
        DeliveryPartner partner = getPartnerByEmail(email);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getDeliveryPartner() == null || !order.getDeliveryPartner().getId().equals(partner.getId())) {
            throw new ForbiddenException("You cannot reject an order that is not assigned to you.");
        }

        order.setDeliveryPartner(null);
        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        Order savedOrder = orderRepository.save(order);

        partner.setStatus(DeliveryPartnerStatus.AVAILABLE);
        partnerRepository.save(partner);

        log.info("Order ID: {} released back to ready pool", orderId);
        return orderMapper.toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getDeliveryHistory(String email, Pageable pageable) {
        DeliveryPartner partner = getPartnerByEmail(email);
        return orderRepository.findByDeliveryPartnerIdOrderByPlacedAtDesc(partner.getId(), pageable)
                .map(orderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public EarningsResponse getEarnings(String email) {
        DeliveryPartner partner = getPartnerByEmail(email);

        // Fetch all orders assigned to rider that are completed (DELIVERED)
        List<Order> deliveries = orderRepository.findByDeliveryPartnerIdAndStatusIn(partner.getId(), List.of(OrderStatus.DELIVERED));

        BigDecimal totalEarnings = deliveries.stream()
                .map(order -> order.getDeliveryFee() != null ? order.getDeliveryFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return EarningsResponse.builder()
                .totalEarnings(totalEarnings)
                .totalDeliveries(deliveries.size())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getPendingDeliveries(Pageable pageable) {
        return orderRepository.findByStatus(OrderStatus.READY_FOR_PICKUP, pageable)
                .map(orderMapper::toResponse);
    }

    // ==================== Helpers ====================

    private DeliveryPartner getPartnerByEmail(String email) {
        return partnerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPartner", "email", email));
    }
}
