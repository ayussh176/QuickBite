package com.quickbite.backend.auth.service;

import com.quickbite.backend.auth.dto.*;
import com.quickbite.backend.auth.entity.PasswordResetToken;
import com.quickbite.backend.auth.entity.User;
import com.quickbite.backend.auth.repository.PasswordResetTokenRepository;
import com.quickbite.backend.auth.repository.UserRepository;
import com.quickbite.backend.cart.entity.Cart;
import com.quickbite.backend.cart.repository.CartRepository;
import com.quickbite.backend.common.enums.AccountStatus;
import com.quickbite.backend.common.enums.Role;
import com.quickbite.backend.customer.entity.Customer;
import com.quickbite.backend.customer.repository.CustomerRepository;
import com.quickbite.backend.delivery.entity.DeliveryPartner;
import com.quickbite.backend.delivery.repository.DeliveryPartnerRepository;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ConflictException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.restaurant.entity.Restaurant;
import com.quickbite.backend.restaurant.repository.RestaurantRepository;
import com.quickbite.backend.security.JwtTokenProvider;
import com.quickbite.backend.utils.SlugUtils;
import com.quickbite.backend.wallet.entity.Wallet;
import com.quickbite.backend.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final WalletRepository walletRepository;
    private final CartRepository cartRepository;
    private final PasswordResetTokenRepository tokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Processing registration request for email: {}", request.getEmail());

        if (request.getRole() == Role.ADMIN) {
            log.error("Registration failed: ADMIN role registration is prohibited");
            throw new BadRequestException("Registration for ADMIN role is not permitted.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered.");
        }

        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new ConflictException("Phone number is already registered.");
        }

        // Create User
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(false)
                .phoneVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        // Build role-specific profiles
        String name = "";
        switch (request.getRole()) {
            case CUSTOMER:
                if (request.getFirstName() == null || request.getLastName() == null) {
                    throw new BadRequestException("First name and Last name are required for Customer.");
                }
                Customer customer = Customer.builder()
                        .user(savedUser)
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .build();
                customerRepository.save(customer);
                name = customer.getFirstName() + " " + customer.getLastName();

                // Create default Cart
                Cart cart = Cart.builder()
                        .customer(customer)
                        .totalAmount(BigDecimal.ZERO)
                        .build();
                cartRepository.save(cart);
                break;

            case RESTAURANT:
                if (request.getRestaurantName() == null || request.getRestaurantName().isBlank()) {
                    throw new BadRequestException("Restaurant name is required.");
                }
                Restaurant restaurant = Restaurant.builder()
                        .user(savedUser)
                        .name(request.getRestaurantName())
                        .slug(SlugUtils.toSlug(request.getRestaurantName()) + "-" + UUID.randomUUID().toString().substring(0, 8))
                        .cuisineType(request.getCuisineType())
                        .email(request.getEmail())
                        .phone(request.getPhone())
                        .build();
                restaurantRepository.save(restaurant);
                name = restaurant.getName();
                break;

            case DELIVERY:
                if (request.getFirstName() == null || request.getLastName() == null) {
                    throw new BadRequestException("First name and Last name are required for Delivery Partner.");
                }
                DeliveryPartner partner = DeliveryPartner.builder()
                        .user(savedUser)
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .phone(request.getPhone())
                        .build();
                deliveryPartnerRepository.save(partner);
                name = partner.getFirstName() + " " + partner.getLastName();
                break;

            default:
                throw new BadRequestException("Invalid role selected.");
        }

        // Initialize Wallet for all roles
        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .balance(BigDecimal.ZERO)
                .build();
        walletRepository.save(wallet);

        log.info("User registered successfully with ID: {}, role: {}", savedUser.getId(), savedUser.getRole());

        return UserResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .role(savedUser.getRole())
                .accountStatus(savedUser.getAccountStatus())
                .emailVerified(savedUser.isEmailVerified())
                .phoneVerified(savedUser.isPhoneVerified())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login request for email: {}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        String accessToken = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        String displayName = "User";
        if (user.getRole() == Role.CUSTOMER) {
            displayName = customerRepository.findByUserId(user.getId())
                    .map(c -> c.getFirstName() + " " + c.getLastName())
                    .orElse("Customer");
        } else if (user.getRole() == Role.RESTAURANT) {
            displayName = restaurantRepository.findByUserId(user.getId())
                    .map(Restaurant::getName)
                    .orElse("Restaurant");
        } else if (user.getRole() == Role.DELIVERY) {
            displayName = deliveryPartnerRepository.findByUserId(user.getId())
                    .map(d -> d.getFirstName() + " " + d.getLastName())
                    .orElse("Delivery Partner");
        } else if (user.getRole() == Role.ADMIN) {
            displayName = "Administrator";
        }

        log.info("User {} logged in successfully", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .role(user.getRole())
                .name(displayName)
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(token)) {
            throw new BadRequestException("Invalid or expired refresh token.");
        }

        String email = jwtTokenProvider.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Generate new tokens
        Map<String, Object> claims = java.util.Map.of("role", "ROLE_" + user.getRole().name());
        String newAccessToken = jwtTokenProvider.generateToken(claims, user.getEmail());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        String displayName = "User";
        if (user.getRole() == Role.CUSTOMER) {
            displayName = customerRepository.findByUserId(user.getId())
                    .map(c -> c.getFirstName() + " " + c.getLastName())
                    .orElse("Customer");
        } else if (user.getRole() == Role.RESTAURANT) {
            displayName = restaurantRepository.findByUserId(user.getId())
                    .map(Restaurant::getName)
                    .orElse("Restaurant");
        } else if (user.getRole() == Role.DELIVERY) {
            displayName = deliveryPartnerRepository.findByUserId(user.getId())
                    .map(d -> d.getFirstName() + " " + d.getLastName())
                    .orElse("Delivery Partner");
        }

        log.info("Refreshed access token for user: {}", email);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .email(user.getEmail())
                .role(user.getRole())
                .name(displayName)
                .build();
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Password reset requested for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        // Delete any existing tokens for the user
        tokenRepository.deleteByUserId(user.getId());

        // Generate token (expires in 15 minutes)
        String resetToken = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .user(user)
                .token(resetToken)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();

        tokenRepository.save(passwordResetToken);

        // Print reset URL to logs (in real apps, send email)
        String resetUrl = "http://localhost:5173/auth/reset-password?token=" + resetToken;
        log.info("=== PASSWORD RESET LINK GENERATED ===");
        log.info("To reset password for {}, click: {}", user.getEmail(), resetUrl);
        log.info("=====================================");
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Resetting password using reset token");

        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid reset token."));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new BadRequestException("Reset token has expired.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete the used token
        tokenRepository.delete(resetToken);

        log.info("Password reset successfully for user: {}", user.getEmail());
    }
}
