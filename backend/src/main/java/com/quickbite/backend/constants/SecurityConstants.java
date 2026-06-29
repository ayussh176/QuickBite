package com.quickbite.backend.constants;

/**
 * Security-related constants.
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // utility class
    }

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String TOKEN_TYPE = "JWT";

    // Public endpoints that bypass authentication
    public static final String[] PUBLIC_URLS = {
            "/v1/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v1/uploads/images/**",
            "/actuator/health",
            "/actuator/info"
    };
}
