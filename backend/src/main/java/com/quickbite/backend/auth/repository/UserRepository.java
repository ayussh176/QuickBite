package com.quickbite.backend.auth.repository;

import com.quickbite.backend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    org.springframework.data.domain.Page<User> findByRoleAndAccountStatus(com.quickbite.backend.common.enums.Role role, com.quickbite.backend.common.enums.AccountStatus accountStatus, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<User> findByRole(com.quickbite.backend.common.enums.Role role, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<User> findByAccountStatus(com.quickbite.backend.common.enums.AccountStatus accountStatus, org.springframework.data.domain.Pageable pageable);
}
