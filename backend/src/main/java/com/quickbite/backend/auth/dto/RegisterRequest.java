package com.quickbite.backend.auth.dto;

import com.quickbite.backend.common.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 15, message = "Phone must be between 10 and 15 digits")
    private String phone;

    @NotNull(message = "Role is required")
    private Role role;

    // Role-specific fields (Conditional Validation)
    
    // Customer / Delivery Partner
    private String firstName;
    private String lastName;

    // Restaurant
    private String restaurantName;
    private String cuisineType;
    private String fssaiLicense;
    private String gstNumber;
    private String restaurantDescription;
    private String address;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private Double latitude;
    private Double longitude;
}
