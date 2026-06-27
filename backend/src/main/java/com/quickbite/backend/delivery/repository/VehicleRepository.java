package com.quickbite.backend.delivery.repository;

import com.quickbite.backend.delivery.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByDeliveryPartnerId(Long deliveryPartnerId);
    boolean existsByRegistrationNumber(String registrationNumber);
}
