package com.quickbite.backend.auth.controller;

import com.quickbite.backend.auth.dto.*;
import com.quickbite.backend.auth.service.AuthService;
import com.quickbite.backend.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Controller", description = "Endpoints for user registration, login, token refresh, and password management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a user account and sets up corresponding Customer, Restaurant, or Delivery Partner profile along with a default wallet and cart")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request for email: {}", request.getEmail());
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully. Please verify your email or phone.", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates credentials and returns JWT access and refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Received login request for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful.", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT access token", description = "Uses a valid refresh token to generate a new pair of access and refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Received token refresh request");
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully.", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logs out the authenticated user by invalidating credentials/sessions on client-side")
    public ResponseEntity<ApiResponse<Void>> logout() {
        log.info("Processing logout request");
        // Stateless logout - handled on client-side by dropping tokens, but provided as endpoint for consistency
        return ResponseEntity.ok(ApiResponse.success("Logout successful. Please delete your client-side tokens."));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Checks if email exists and generates a secure password reset token printed to system logs")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Received forgot password request for email: {}", request.getEmail());
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("If the email exists, a password reset link has been generated. Check system logs."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset user password", description = "Resets the password using a valid, non-expired password reset token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Received reset password request");
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully. Please login with your new password."));
    }
}
