package com.quickbite.backend.security;

import com.quickbite.backend.auth.entity.User;
import com.quickbite.backend.auth.repository.UserRepository;
import com.quickbite.backend.common.enums.AccountStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login attempt failed: user with email {} not found", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        boolean enabled = user.getAccountStatus() != AccountStatus.INACTIVE
                && user.getAccountStatus() != AccountStatus.SUSPENDED;
        boolean accountNonLocked = user.getAccountStatus() != AccountStatus.SUSPENDED;

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                enabled,
                true, // accountNonExpired
                true, // credentialsNonExpired
                accountNonLocked,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
