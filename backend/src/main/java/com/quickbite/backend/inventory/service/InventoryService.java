package com.quickbite.backend.inventory.service;

import com.quickbite.backend.exception.ForbiddenException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.inventory.dto.InventoryResponse;
import com.quickbite.backend.inventory.dto.UpdateStockRequest;
import com.quickbite.backend.inventory.entity.Inventory;
import com.quickbite.backend.inventory.mapper.InventoryMapper;
import com.quickbite.backend.inventory.repository.InventoryRepository;
import com.quickbite.backend.menu.entity.FoodItem;
import com.quickbite.backend.menu.repository.FoodItemRepository;
import com.quickbite.backend.restaurant.entity.Restaurant;
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
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final FoodItemRepository foodItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final InventoryMapper inventoryMapper;

    @Transactional
    public InventoryResponse updateStock(String ownerEmail, Long itemId, UpdateStockRequest request) {
        log.info("Updating stock level for item ID: {} by owner: {}", itemId, ownerEmail);
        Restaurant restaurant = getRestaurantByOwnerEmail(ownerEmail);
        FoodItem foodItem = getFoodItemById(itemId);

        validateRestaurantOwnership(restaurant, foodItem.getRestaurant().getId());

        // Find or build inventory record
        Inventory inventory = inventoryRepository.findByFoodItemId(itemId)
                .orElseGet(() -> Inventory.builder().foodItem(foodItem).build());

        inventory.setQuantity(request.getQuantity());
        inventory.setLowStockThreshold(request.getLowStockThreshold());

        Inventory savedInventory = inventoryRepository.save(inventory);
        log.info("Stock level updated for item ID: {}. New Quantity: {}", itemId, savedInventory.getQuantity());

        return inventoryMapper.toResponse(savedInventory);
    }

    @Transactional(readOnly = true)
    public Page<InventoryResponse> getLowStockItems(String ownerEmail, Pageable pageable) {
        log.info("Fetching low-stock items for restaurant owner: {}", ownerEmail);
        Restaurant restaurant = getRestaurantByOwnerEmail(ownerEmail);
        return inventoryRepository.findLowStockItemsByRestaurantId(restaurant.getId(), pageable)
                .map(inventoryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<InventoryResponse> getInventory(String ownerEmail, Pageable pageable) {
        log.info("Fetching full inventory for restaurant owner: {}", ownerEmail);
        Restaurant restaurant = getRestaurantByOwnerEmail(ownerEmail);
        return inventoryRepository.findByRestaurantId(restaurant.getId(), pageable)
                .map(inventoryMapper::toResponse);
    }

    // ==================== Helpers ====================

    private Restaurant getRestaurantByOwnerEmail(String ownerEmail) {
        return restaurantRepository.findByUserEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant owned by " + ownerEmail, "email", ownerEmail));
    }

    private FoodItem getFoodItemById(Long itemId) {
        return foodItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("FoodItem", "id", itemId));
    }

    private void validateRestaurantOwnership(Restaurant ownerRestaurant, Long targetRestaurantId) {
        if (!ownerRestaurant.getId().equals(targetRestaurantId)) {
            log.error("Access denied: Restaurant ID {} does not own target ID {}", ownerRestaurant.getId(), targetRestaurantId);
            throw new ForbiddenException("You do not have permission to manage this stock.");
        }
    }
}
