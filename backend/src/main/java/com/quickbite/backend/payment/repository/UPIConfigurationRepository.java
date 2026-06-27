package com.quickbite.backend.payment.repository;

import com.quickbite.backend.payment.entity.UPIConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UPIConfigurationRepository extends JpaRepository<UPIConfiguration, Long> {
    List<UPIConfiguration> findByRestaurantId(Long restaurantId);
    Optional<UPIConfiguration> findByRestaurantIdAndIsDefaultTrue(Long restaurantId);
    Optional<UPIConfiguration> findByIdAndRestaurantId(Long id, Long restaurantId);
}
