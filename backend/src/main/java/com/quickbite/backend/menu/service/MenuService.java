package com.quickbite.backend.menu.service;

import com.quickbite.backend.common.enums.FoodType;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ForbiddenException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.menu.dto.CategoryRequest;
import com.quickbite.backend.menu.dto.CategoryResponse;
import com.quickbite.backend.menu.dto.FoodItemRequest;
import com.quickbite.backend.menu.dto.FoodItemResponse;
import com.quickbite.backend.menu.entity.FoodCategory;
import com.quickbite.backend.menu.entity.FoodImage;
import com.quickbite.backend.menu.entity.FoodItem;
import com.quickbite.backend.menu.mapper.FoodCategoryMapper;
import com.quickbite.backend.menu.mapper.FoodItemMapper;
import com.quickbite.backend.menu.repository.FoodCategoryRepository;
import com.quickbite.backend.menu.repository.FoodImageRepository;
import com.quickbite.backend.menu.repository.FoodItemRepository;
import com.quickbite.backend.restaurant.entity.Restaurant;
import com.quickbite.backend.restaurant.repository.RestaurantRepository;
import com.quickbite.backend.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final FoodCategoryRepository categoryRepository;
    private final FoodItemRepository foodItemRepository;
    private final FoodImageRepository foodImageRepository;
    private final RestaurantRepository restaurantRepository;

    private final FoodCategoryMapper categoryMapper;
    private final FoodItemMapper foodItemMapper;

    // ==================== Category Operations ====================

    @Transactional
    public CategoryResponse createCategory(String ownerEmail, CategoryRequest request) {
        log.info("Creating category for restaurant owner: {}", ownerEmail);
        Restaurant restaurant = getRestaurantByOwnerEmail(ownerEmail);

        if (categoryRepository.existsByRestaurantIdAndName(restaurant.getId(), request.getName())) {
            throw new BadRequestException("Category already exists with name: " + request.getName());
        }

        FoodCategory category = categoryMapper.toEntity(request);
        category.setRestaurant(restaurant);

        FoodCategory savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());
        return categoryMapper.toResponse(savedCategory);
    }

    @Transactional
    public CategoryResponse updateCategory(String ownerEmail, Long categoryId, CategoryRequest request) {
        log.info("Updating category ID: {} for owner: {}", categoryId, ownerEmail);
        Restaurant restaurant = getRestaurantByOwnerEmail(ownerEmail);
        FoodCategory category = getCategoryById(categoryId);

        validateRestaurantOwnership(restaurant, category.getRestaurant().getId());

        categoryMapper.updateEntityFromRequest(request, category);
        FoodCategory savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    @Transactional
    public void deleteCategory(String ownerEmail, Long categoryId) {
        log.info("Deleting category ID: {} for owner: {}", categoryId, ownerEmail);
        Restaurant restaurant = getRestaurantByOwnerEmail(ownerEmail);
        FoodCategory category = getCategoryById(categoryId);

        validateRestaurantOwnership(restaurant, category.getRestaurant().getId());

        categoryRepository.delete(category);
        log.info("Category ID: {} deleted successfully", categoryId);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByRestaurant(Long restaurantId) {
        return categoryRepository.findByRestaurantIdOrderBySortOrderAsc(restaurantId)
                .stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== Food Item Operations ====================

    @Transactional
    public FoodItemResponse createFoodItem(String ownerEmail, FoodItemRequest request) {
        log.info("Creating food item for restaurant owner: {}", ownerEmail);
        Restaurant restaurant = getRestaurantByOwnerEmail(ownerEmail);

        FoodCategory category = getCategoryById(request.getCategoryId());
        validateRestaurantOwnership(restaurant, category.getRestaurant().getId());

        if (foodItemRepository.existsByRestaurantIdAndName(restaurant.getId(), request.getName())) {
            throw new BadRequestException("Food item already exists with name: " + request.getName());
        }

        FoodItem foodItem = foodItemMapper.toEntity(request);
        foodItem.setRestaurant(restaurant);
        foodItem.setCategory(category);
        foodItem.setSlug(SlugUtils.toSlug(request.getName()) + "-" + UUID.randomUUID().toString().substring(0, 8));

        // Save food item first
        FoodItem savedItem = foodItemRepository.save(foodItem);

        // Process images if provided
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            request.getImages().forEach(imgDto -> {
                FoodImage img = foodItemMapper.toImageEntity(imgDto);
                savedItem.addImage(img);
            });
            foodItemRepository.save(savedItem);
        }

        log.info("Food item created successfully with ID: {}", savedItem.getId());
        return foodItemMapper.toResponse(savedItem);
    }

    @Transactional
    public FoodItemResponse updateFoodItem(String ownerEmail, Long itemId, FoodItemRequest request) {
        log.info("Updating food item ID: {} for owner: {}", itemId, ownerEmail);
        Restaurant restaurant = getRestaurantByOwnerEmail(ownerEmail);
        FoodItem foodItem = getFoodItemById(itemId);

        validateRestaurantOwnership(restaurant, foodItem.getRestaurant().getId());

        FoodCategory category = getCategoryById(request.getCategoryId());
        validateRestaurantOwnership(restaurant, category.getRestaurant().getId());

        foodItemMapper.updateEntityFromRequest(request, foodItem);
        foodItem.setCategory(category);

        // Sync images (clear and recreate for simplicity)
        if (request.getImages() != null) {
            foodItem.getImages().clear();
            request.getImages().forEach(imgDto -> {
                FoodImage img = foodItemMapper.toImageEntity(imgDto);
                foodItem.addImage(img);
            });
        }

        FoodItem savedItem = foodItemRepository.save(foodItem);
        return foodItemMapper.toResponse(savedItem);
    }

    @Transactional
    public void deleteFoodItem(String ownerEmail, Long itemId) {
        log.info("Deleting food item ID: {} for owner: {}", itemId, ownerEmail);
        Restaurant restaurant = getRestaurantByOwnerEmail(ownerEmail);
        FoodItem foodItem = getFoodItemById(itemId);

        validateRestaurantOwnership(restaurant, foodItem.getRestaurant().getId());

        foodItemRepository.delete(foodItem);
        log.info("Food item ID: {} deleted successfully", itemId);
    }

    @Transactional(readOnly = true)
    public Page<FoodItemResponse> getMenu(Long restaurantId, Long categoryId, FoodType foodType, Pageable pageable) {
        if (categoryId != null && foodType != null) {
            return foodItemRepository.findAll((root, query, cb) -> cb.and(
                    cb.equal(root.get("restaurant").get("id"), restaurantId),
                    cb.equal(root.get("category").get("id"), categoryId),
                    cb.equal(root.get("foodType"), foodType),
                    cb.equal(root.get("available"), true)
            ), pageable).map(foodItemMapper::toResponse);
        } else if (categoryId != null) {
            return foodItemRepository.findByRestaurantIdAndCategoryIdAndAvailable(restaurantId, categoryId, true, pageable)
                    .map(foodItemMapper::toResponse);
        } else if (foodType != null) {
            return foodItemRepository.findByRestaurantIdAndFoodTypeAndAvailable(restaurantId, foodType, true, pageable)
                    .map(foodItemMapper::toResponse);
        } else {
            return foodItemRepository.findByRestaurantIdAndAvailable(restaurantId, true, pageable)
                    .map(foodItemMapper::toResponse);
        }
    }

    @Transactional(readOnly = true)
    public FoodItemResponse getFoodItemDetails(Long itemId) {
        return foodItemMapper.toResponse(getFoodItemById(itemId));
    }

    // ==================== Helper Methods ====================

    private Restaurant getRestaurantByOwnerEmail(String ownerEmail) {
        return restaurantRepository.findByUserEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant owned by " + ownerEmail, "email", ownerEmail));
    }

    private FoodCategory getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("FoodCategory", "id", categoryId));
    }

    private FoodItem getFoodItemById(Long itemId) {
        return foodItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("FoodItem", "id", itemId));
    }

    private void validateRestaurantOwnership(Restaurant ownerRestaurant, Long targetRestaurantId) {
        if (!ownerRestaurant.getId().equals(targetRestaurantId)) {
            log.error("Access denied: Restaurant ID {} does not own target ID {}", ownerRestaurant.getId(), targetRestaurantId);
            throw new ForbiddenException("You do not have permission to manage this menu.");
        }
    }
}
