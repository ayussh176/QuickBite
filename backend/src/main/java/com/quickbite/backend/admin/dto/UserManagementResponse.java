package com.quickbite.backend.admin.dto;

import com.quickbite.backend.common.enums.AccountStatus;
import com.quickbite.backend.common.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementResponse {

    private Long id;
    private String email;
    private String phone;
    private Role role;
    private AccountStatus status;
    private LocalDateTime createdAt;
}
