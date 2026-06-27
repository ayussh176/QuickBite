package com.quickbite.backend.restaurant.service;

import com.quickbite.backend.common.enums.RestaurantStatus;
import com.quickbite.backend.exception.ForbiddenException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.restaurant.dto.RestaurantRequest;
import com.quickbite.backend.restaurant.dto.RestaurantResponse;
import com.quickbite.backend.restaurant.entity.Restaurant;
import com.quickbite.backend.restaurant.entity.RestaurantAddress;
import com.quickbite.backend.restaurant.mapper.RestaurantMapper;
import com.quickbite.backend.restaurant.repository.RestaurantAddressRepository;
import com.quickbite.backend.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantAddressRepository addressRepository;
    private final RestaurantMapper restaurantMapper;

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
        return restaurantMapper.toResponse(restaurant);
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantBySlug(String slug) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "slug", slug));
        return restaurantMapper.toResponse(restaurant);
    }

    @Transactional(readOnly = true)
    public Page<RestaurantResponse> getAllRestaurants(Pageable pageable) {
        return restaurantRepository.findAll(pageable)
                .map(restaurantMapper::toResponse);
    }

    @Transactional
    public RestaurantResponse updateRestaurantProfile(String ownerEmail, RestaurantRequest request) {
        log.info("Updating profile for restaurant owned by: {}", ownerEmail);

        Restaurant restaurant = restaurantRepository.findByUserEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant owned by " + ownerEmail, "email", ownerEmail));

        // Update fields
        restaurantMapper.updateEntityFromRequest(request, restaurant);

        // Handle address update
        if (request.getAddress() != null) {
            if (restaurant.getAddress() == null) {
                RestaurantAddress newAddress = restaurantMapper.toAddressEntity(request.getAddress());
                restaurant.setAddress(newAddress);
            } else {
                restaurantMapper.updateAddressEntity(request.getAddress(), restaurant.getAddress());
            }
        }

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        log.info("Profile updated successfully for restaurant ID: {}", savedRestaurant.getId());

        return restaurantMapper.toResponse(savedRestaurant);
    }

    @Transactional
    public RestaurantResponse updateRestaurantStatus(Long id, RestaurantStatus status) {
        log.info("Updating status of restaurant ID: {} to {}", id, status);

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));

        restaurant.setStatus(status);
        if (status == RestaurantStatus.APPROVED) {
            restaurant.setActive(true);
        } else {
            restaurant.setActive(false);
        }

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant ID: {} status updated to {}", id, status);

        return restaurantMapper.toResponse(savedRestaurant);
    }
}
