package com.quickbite.backend.delivery.repository;

import com.quickbite.backend.delivery.entity.DeliveryPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {
    Optional<DeliveryPartner> findByUserId(Long userId);
    Optional<DeliveryPartner> findByUserEmail(String email);
}
