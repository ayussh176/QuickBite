package com.quickbite.backend.admin.repository;

import com.quickbite.backend.admin.entity.Complaint;
import com.quickbite.backend.common.enums.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    Page<Complaint> findByStatus(ComplaintStatus status, Pageable pageable);
    List<Complaint> findByCustomerId(Long customerId);
}
