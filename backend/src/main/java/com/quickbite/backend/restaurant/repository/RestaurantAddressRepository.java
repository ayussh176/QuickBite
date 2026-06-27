package com.quickbite.backend.restaurant.repository;

import com.quickbite.backend.restaurant.entity.RestaurantAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantAddressRepository extends JpaRepository<RestaurantAddress, Long> {
}
