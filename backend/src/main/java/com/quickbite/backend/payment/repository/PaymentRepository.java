package com.quickbite.backend.payment.repository;

import com.quickbite.backend.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByTransactionId(String transactionId);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Payment p JOIN p.order o WHERE o.customer.id = :customerId")
    org.springframework.data.domain.Page<Payment> findByCustomerId(Long customerId, org.springframework.data.domain.Pageable pageable);
}
