package com.quickbite.backend.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final CacheManager cacheManager;

    public void evictWallet(String email) {
        log.info("Evicting wallet cache for: {}", email);
        evict("wallets", email);
    }

    public void evictRestaurantList() {
        log.info("Evicting all restaurant list caches");
        Cache cache = cacheManager.getCache("restaurants");
        if (cache != null) {
            cache.clear();
        }
    }

    public void evictRestaurant(Long id) {
        log.info("Evicting restaurant cache for ID: {}", id);
        evict("restaurants", id);
        evictRestaurantList();
    }

    public void evictMenu(Long restaurantId) {
        log.info("Evicting menu cache for restaurant ID: {}", restaurantId);
        evict("menu", restaurantId);
    }

    public void evictCategories(Long restaurantId) {
        log.info("Evicting categories cache for restaurant ID: {}", restaurantId);
        evict("categories", restaurantId);
    }

    public void evictCoupons(Long restaurantId) {
        log.info("Evicting coupons cache for restaurant ID: {}", restaurantId);
        evict("coupons", restaurantId + "-false");
        evict("coupons", "null-true"); // Evict global coupons
        evict("coupons", "all");
        Cache cache = cacheManager.getCache("coupons");
        if (cache != null) {
            cache.clear();
        }
    }

    private void evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }
}
