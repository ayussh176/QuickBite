package com.quickbite.backend.auth.dto;

import com.quickbite.backend.common.enums.AccountStatus;
import com.quickbite.backend.common.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String phone;
    private Role role;
    private AccountStatus accountStatus;
    private boolean emailVerified;
    private boolean phoneVerified;
}
