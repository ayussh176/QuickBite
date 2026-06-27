package com.quickbite.backend.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Placeholder UserDetailsService.
 * <p>
 * This will be replaced with a real implementation that loads users
 * from the database once the auth module entities are created.
 * </p>
 */
@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO: Replace with actual database lookup from auth module
        log.warn("Using placeholder UserDetailsService — no users in database yet");
        throw new UsernameNotFoundException("User not found with email: " + username);
    }
}
