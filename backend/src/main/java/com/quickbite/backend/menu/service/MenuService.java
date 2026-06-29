package com.quickbite.backend.menu.service;

import com.quickbite.backend.common.enums.FoodType;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ForbiddenException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.inventory.entity.Inventory;
import com.quickbite.backend.inventory.repository.InventoryRepository;
import com.quickbite.backend.menu.dto.CategoryRequest;
import com.quickbite.backend.menu.dto.CategoryResponse;
import com.quickbite.backend.menu.dto.FoodItemRequest;
import com.quickbite.backend.menu.dto.FoodItemResponse;
import com.quickbite.backend.menu.entity.FoodCategory;
import com.quickbite.backend.menu.entity.FoodImage;
import com.quickbite.backend.menu.entity.FoodItem;
import com.quickbite.backend.menu.entity.FoodItemAddOn;
import com.quickbite.backend.menu.entity.FoodItemVariant;
import com.quickbite.backend.menu.mapper.FoodCategoryMapper;
import com.quickbite.backend.menu.mapper.FoodItemMapper;
import com.quickbite.backend.menu.repository.FoodCategoryRepository;
import com.quickbite.backend.menu.repository.FoodImageRepository;
import com.quickbite.backend.menu.repository.FoodItemRepository;
import com.quickbite.backend.restaurant.entity.Restaurant;
import com.quickbite.backend.restaurant.repository.RestaurantRepository;
import com.quickbite.backend.utils.SlugUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    private final InventoryRepository inventoryRepository;

    private final FoodCategoryMapper categoryMapper;
    private final FoodItemMapper foodItemMapper;
    private final com.quickbite.backend.common.service.CacheService cacheService;

    // ==================== Category Operations ====================

    @Transactional
    public CategoryResponse createCategory(String ownerEmail, Long restaurantId, CategoryRequest request) {
        log.info("Creating category for restaurant owner: {}", ownerEmail);
        Restaurant restaurant = getRestaurantByOwnerEmail(ownerEmail);
        validateRestaurantOwnership(restaurant, restaurantId);

        var existingCategory = categoryRepository.findByRestaurantIdAndNameIgnoreCase(restaurant.getId(), request.getName());
        if (existingCategory.isPresent()) {
            FoodCategory category = existingCategory.get();
            if (category.isActive()) {
                throw new BadRequestException("Category already exists with name: " + request.getName());
            }

            categoryMapper.updateEntityFromRequest(request, category);
            normalizeCategory(category, request);
            category.setActive(true);
            FoodCategory savedCategory = categoryRepository.save(category);
            log.info("Category reactivated successfully with ID: {}", savedCategory.getId());
            return categoryMapper.toResponse(savedCategory);
        }

        FoodCategory category = categoryMapper.toEntity(request);
        category.setRestaurant(restaurant);
        normalizeCategory(category, request);

        FoodCategory savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());
        cacheService.evictCategories(restaurantId);
        return categoryMapper.toResponse(savedCategory);
    }

    @Transactional
    public CategoryResponse updateCategory(String ownerEmail, Long restaurantId, Long categoryId, CategoryRequest request) {
        log.info("Updating category ID: {} for owner: {}", categoryId, ownerEmail);
        Restaurant restaurant = getRestaurantByOwnerEmail(ownerEmail);
        validateRestaurantOwnership(restaurant, restaurantId);
        FoodCategory category = getCategoryById(categoryId);

        validateRestaurantOwnership(restaurant, category.getRestaurant().getId());

        categoryMapper.updateEntityFromRequest(request, category);
        normalizeCategory(category, request);
        FoodCategory savedCategory = categoryRepository.save(category);
        cacheService.evictCategories(restaurantId);

        return categoryMapper.toResponse(savedCategory);
    }

    @Transactional
    public void deleteCategory(String ownerEmail, Long restaurantId, Long categoryId) {
        log.info("Deleting category ID: {} for owner: {}", categoryId, ownerEmail);
        Restaurant restaurant = getRestaurantByOwnerEmail(ownerEmail);
        validateRestaurantOwnership(restaurant, restaurantId);
        FoodCategory category = getCategoryById(categoryId);

        validateRestaurantOwnership(restaurant, category.getRestaurant().getId());

        category.setActive(false);
        category.getFoodItems().forEach(item -> item.setAvailable(false));
        categoryRepository.save(category);
        log.info("Category ID: {} deactivated successfully", categoryId);
        cacheService.evictCategories(restaurantId);
        cacheService.evictMenu(restaurantId);
    }

    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "categories", key = "#restaurantId")
    public List<CategoryResponse> getCategoriesByRestaurant(Long restaurantId) {
        return categoryRepository.findByRestaurantIdAndActiveTrueOrderBySortOrderAsc(restaurantId)
                .stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== Food Item Operations ====================

    @Transactional
    public FoodItemResponse createFoodItem(String ownerEmail, Long restaurantId, FoodItemRequest request) {
        log.info("Creating food item for restaurant owner: {}", ownerEmail);
        Restaurant restaurant = getRestaurantByOwnerEmail(ownerEmail);
        validateRestaurantOwnership(restaurant, restaurantId);

        FoodCategory category = getCategoryById(request.getCategoryId());
        validateRestaurantOwnership(restaurant, category.getRestaurant().getId());
        if (!category.isActive()) {
            throw new BadRequestException("Cannot add food items to an inactive category.");
        }

        if (foodItemRepository.existsByRestaurantIdAndName(restaurant.getId(), request.getName())) {
            throw new BadRequestException("Food item already exists with name: " + request.getName());
        }

        FoodItem foodItem = foodItemMapper.toEntity(request);
        foodItem.setRestaurant(restaurant);
        foodItem.setCategory(category);
        foodItem.setSlug(SlugUtils.toSlug(request.getName()) + "-" + UUID.randomUUID().toString().substring(0, 8));
        normalizeFoodItem(foodItem, request);

        // Save food item first
        FoodItem savedItem = foodItemRepository.save(foodItem);

        // Process images
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            request.getImages().forEach(imgDto -> {
                FoodImage img = foodItemMapper.toImageEntity(imgDto);
                savedItem.addImage(img);
            });
        }

        // Process variants
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            request.getVariants().forEach(vDto -> {
                FoodItemVariant variant = foodItemMapper.toVariantEntity(vDto);
                savedItem.addVariant(variant);
            });
        }

        // Process add-ons
        if (request.getAddOns() != null && !request.getAddOns().isEmpty()) {
            request.getAddOns().forEach(aDto -> {
                FoodItemAddOn addOn = foodItemMapper.toAddOnEntity(aDto);
                savedItem.addAddOn(addOn);
            });
        }

        // Save completely assembled item
        FoodItem fullySavedItem = foodItemRepository.save(savedItem);

        // Auto-initialize inventory (default quantity = 0, low-stock threshold = 10)
        Inventory inventory = Inventory.builder()
                .foodItem(fullySavedItem)
                .quantity(0)
                .lowStockThreshold(10)
                .build();
        inventoryRepository.save(inventory);

        log.info("Food item created successfully with ID: {} and default stock initialized", fullySavedItem.getId());
        cacheService.evictMenu(restaurantId);
        return foodItemMapper.toResponse(fullySavedItem);
    }

    @Transactional
    public FoodItemResponse updateFoodItem(String ownerEmail, Long restaurantId, Long itemId, FoodItemRequest request) {
        log.info("Updating food item ID: {} for owner: {}", itemId, ownerEmail);
        Restaurant restaurant = getRestaurantByOwnerEmail(ownerEmail);
        validateRestaurantOwnership(restaurant, restaurantId);
        FoodItem foodItem = getFoodItemById(itemId);

        validateRestaurantOwnership(restaurant, foodItem.getRestaurant().getId());

        FoodCategory category = getCategoryById(request.getCategoryId());
        validateRestaurantOwnership(restaurant, category.getRestaurant().getId());
        if (!category.isActive()) {
            throw new BadRequestException("Cannot move food items to an inactive category.");
        }

        foodItemMapper.updateEntityFromRequest(request, foodItem);
        foodItem.setCategory(category);
        normalizeFoodItem(foodItem, request);

        // Sync images (clear and rebuild)
        if (request.getImages() != null) {
            foodItem.getImages().clear();
            request.getImages().forEach(imgDto -> {
                FoodImage img = foodItemMapper.toImageEntity(imgDto);
                foodItem.addImage(img);
            });
        }

        // Sync variants (clear and rebuild)
        if (request.getVariants() != null) {
            foodItem.getVariants().clear();
            request.getVariants().forEach(vDto -> {
                FoodItemVariant variant = foodItemMapper.toVariantEntity(vDto);
                foodItem.addVariant(variant);
            });
        }

        // Sync add-ons (clear and rebuild)
        if (request.getAddOns() != null) {
            foodItem.getAddOns().clear();
            request.getAddOns().forEach(aDto -> {
                FoodItemAddOn addOn = foodItemMapper.toAddOnEntity(aDto);
                foodItem.addAddOn(addOn);
            });
        }

        FoodItem savedItem = foodItemRepository.save(foodItem);
        log.info("Food item ID: {} updated successfully", savedItem.getId());
        cacheService.evictMenu(restaurantId);
        return foodItemMapper.toResponse(savedItem);
    }

    @Transactional
    public void deleteFoodItem(String ownerEmail, Long restaurantId, Long itemId) {
        log.info("Deleting food item ID: {} for owner: {}", itemId, ownerEmail);
        Restaurant restaurant = getRestaurantByOwnerEmail(ownerEmail);
        validateRestaurantOwnership(restaurant, restaurantId);
        FoodItem foodItem = getFoodItemById(itemId);

        validateRestaurantOwnership(restaurant, foodItem.getRestaurant().getId());

        // Remove associated inventory record first to avoid FK constraint violation
        inventoryRepository.findByFoodItemId(itemId).ifPresent(inventoryRepository::delete);

        foodItem.setAvailable(false);
        foodItemRepository.save(foodItem);
        log.info("Food item ID: {} deactivated successfully", itemId);
        cacheService.evictMenu(restaurantId);
    }

    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "menu", key = "#restaurantId")
    public Page<FoodItemResponse> getMenu(Long restaurantId, Long categoryId, FoodType foodType, Pageable pageable) {
        Specification<FoodItem> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("restaurant").get("id"), restaurantId),
                cb.equal(root.get("available"), true)
        );

        if (categoryId != null) {
            Specification<FoodItem> categorySpec = (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
            spec = spec.and(categorySpec);
        }

        if (foodType != null) {
            Specification<FoodItem> foodTypeSpec = (root, query, cb) -> cb.equal(root.get("foodType"), foodType);
            spec = spec.and(foodTypeSpec);
        }

        return foodItemRepository.findAll(spec, pageable).map(foodItemMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<FoodItemResponse> searchFoodItems(Long restaurantId, String searchKeyword, Long categoryId,
                                                  FoodType foodType, BigDecimal minPrice, BigDecimal maxPrice,
                                                  Boolean bestseller, Pageable pageable) {
        Specification<FoodItem> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("restaurant").get("id"), restaurantId));
            predicates.add(cb.equal(root.get("available"), true));

            if (searchKeyword != null && !searchKeyword.isBlank()) {
                String keyword = "%" + searchKeyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), keyword),
                        cb.like(cb.lower(root.get("description")), keyword)
                ));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (foodType != null) {
                predicates.add(cb.equal(root.get("foodType"), foodType));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (bestseller != null) {
                predicates.add(cb.equal(root.get("bestseller"), bestseller));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return foodItemRepository.findAll(spec, pageable).map(foodItemMapper::toResponse);
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

    private void normalizeCategory(FoodCategory category, CategoryRequest request) {
        if (request.getActive() == null) {
            category.setActive(true);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
    }

    private void normalizeFoodItem(FoodItem foodItem, FoodItemRequest request) {
        if (request.getAvailable() == null) {
            foodItem.setAvailable(true);
        }
        if (request.getBestseller() == null) {
            foodItem.setBestseller(false);
        }
    }
}
