package com.quickbite.backend.customer.service;

import com.quickbite.backend.customer.dto.CustomerAddressDto;
import com.quickbite.backend.customer.dto.CustomerRequest;
import com.quickbite.backend.customer.dto.CustomerResponse;
import com.quickbite.backend.customer.entity.Customer;
import com.quickbite.backend.customer.entity.CustomerAddress;
import com.quickbite.backend.customer.mapper.CustomerMapper;
import com.quickbite.backend.customer.repository.CustomerAddressRepository;
import com.quickbite.backend.customer.repository.CustomerRepository;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ForbiddenException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.menu.dto.FoodItemResponse;
import com.quickbite.backend.menu.entity.FoodItem;
import com.quickbite.backend.menu.mapper.FoodItemMapper;
import com.quickbite.backend.menu.repository.FoodItemRepository;
import com.quickbite.backend.order.entity.Order;
import com.quickbite.backend.order.repository.OrderRepository;
import com.quickbite.backend.restaurant.dto.RestaurantResponse;
import com.quickbite.backend.restaurant.entity.Restaurant;
import com.quickbite.backend.restaurant.mapper.RestaurantMapper;
import com.quickbite.backend.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final FoodItemRepository foodItemRepository;

    private final CustomerMapper customerMapper;
    private final RestaurantMapper restaurantMapper;
    private final FoodItemMapper foodItemMapper;

    // ==================== Profile Operations ====================

    @Transactional(readOnly = true)
    public CustomerResponse getProfile(String email) {
        Customer customer = getCustomerByEmail(email);
        return customerMapper.toResponse(customer);
    }

    @Transactional
    public CustomerResponse updateProfile(String email, CustomerRequest request) {
        log.info("Updating customer profile for email: {}", email);
        Customer customer = getCustomerByEmail(email);

        customerMapper.updateEntityFromRequest(request, customer);
        Customer savedCustomer = customerRepository.save(customer);

        return customerMapper.toResponse(savedCustomer);
    }

    // ==================== Address Operations ====================

    @Transactional
    public CustomerAddressDto addAddress(String email, CustomerAddressDto addressDto) {
        log.info("Adding new address for customer: {}", email);
        Customer customer = getCustomerByEmail(email);

        CustomerAddress address = customerMapper.toAddressEntity(addressDto);
        address.setCustomer(customer);

        List<CustomerAddress> existingAddresses = addressRepository.findByCustomerId(customer.getId());
        if (existingAddresses.isEmpty()) {
            address.setDefault(true);
        } else if (address.isDefault()) {
            resetDefaultAddresses(customer.getId());
        }

        CustomerAddress savedAddress = addressRepository.save(address);
        log.info("Address added successfully with ID: {}", savedAddress.getId());

        return customerMapper.toAddressDto(savedAddress);
    }

    @Transactional
    public CustomerAddressDto updateAddress(String email, Long addressId, CustomerAddressDto addressDto) {
        log.info("Updating address ID: {} for customer: {}", addressId, email);
        Customer customer = getCustomerByEmail(email);
        CustomerAddress address = getAddressByIdAndCustomer(addressId, customer.getId());

        customerMapper.updateAddressEntityFromDto(addressDto, address);

        if (addressDto.isDefault()) {
            resetDefaultAddresses(customer.getId());
            address.setDefault(true);
        }

        CustomerAddress savedAddress = addressRepository.save(address);
        return customerMapper.toAddressDto(savedAddress);
    }

    @Transactional
    public void deleteAddress(String email, Long addressId) {
        log.info("Deleting address ID: {} for customer: {}", addressId, email);
        Customer customer = getCustomerByEmail(email);
        CustomerAddress address = getAddressByIdAndCustomer(addressId, customer.getId());

        if (address.isDefault()) {
            addressRepository.delete(address);
            // Elect a new default address if any addresses remain
            List<CustomerAddress> remaining = addressRepository.findByCustomerId(customer.getId());
            if (!remaining.isEmpty()) {
                CustomerAddress newDefault = remaining.get(0);
                newDefault.setDefault(true);
                addressRepository.save(newDefault);
            }
        } else {
            addressRepository.delete(address);
        }
        log.info("Address ID: {} deleted successfully", addressId);
    }

    @Transactional(readOnly = true)
    public List<CustomerAddressDto> getAddresses(String email) {
        Customer customer = getCustomerByEmail(email);
        return addressRepository.findByCustomerId(customer.getId())
                .stream()
                .map(customerMapper::toAddressDto)
                .collect(Collectors.toList());
    }

    // ==================== Saved Restaurants Operations ====================

    @Transactional
    public void toggleSavedRestaurant(String email, Long restaurantId) {
        Customer customer = getCustomerByEmail(email);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        if (customer.getSavedRestaurants().contains(restaurant)) {
            customer.getSavedRestaurants().remove(restaurant);
            log.info("Removed restaurant ID: {} from saved list of customer: {}", restaurantId, email);
        } else {
            customer.getSavedRestaurants().add(restaurant);
            log.info("Added restaurant ID: {} to saved list of customer: {}", restaurantId, email);
        }
        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getSavedRestaurants(String email) {
        Customer customer = getCustomerByEmail(email);
        return customer.getSavedRestaurants()
                .stream()
                .map(restaurantMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== Wishlist Operations ====================

    @Transactional
    public void toggleWishlistItem(String email, Long foodItemId) {
        Customer customer = getCustomerByEmail(email);
        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new ResourceNotFoundException("FoodItem", "id", foodItemId));

        if (customer.getWishlist().contains(foodItem)) {
            customer.getWishlist().remove(foodItem);
            log.info("Removed item ID: {} from wishlist of customer: {}", foodItemId, email);
        } else {
            customer.getWishlist().add(foodItem);
            log.info("Added item ID: {} to wishlist of customer: {}", foodItemId, email);
        }
        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public List<FoodItemResponse> getWishlist(String email) {
        Customer customer = getCustomerByEmail(email);
        return customer.getWishlist()
                .stream()
                .map(foodItemMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== Order History ====================

    @Transactional(readOnly = true)
    public Page<Order> getOrderHistory(String email, Pageable pageable) {
        Customer customer = getCustomerByEmail(email);
        return orderRepository.findByCustomerIdOrderByPlacedAtDesc(customer.getId(), pageable);
    }

    // ==================== Helper Methods ====================

    private Customer getCustomerByEmail(String email) {
        return customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
    }

    private CustomerAddress getAddressByIdAndCustomer(Long addressId, Long customerId) {
        return addressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerAddress", "id", addressId));
    }

    private void resetDefaultAddresses(Long customerId) {
        List<CustomerAddress> addresses = addressRepository.findByCustomerId(customerId);
        addresses.forEach(addr -> {
            if (addr.isDefault()) {
                addr.setDefault(false);
                addressRepository.save(addr);
            }
        });
    }
}
