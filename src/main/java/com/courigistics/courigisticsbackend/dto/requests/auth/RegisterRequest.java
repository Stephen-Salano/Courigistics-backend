package com.courigistics.courigisticsbackend.dto.requests.auth;

import com.courigistics.courigisticsbackend.dto.requests.AddressDTO;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank(message = "Email cannot be blank")
        @Email
        String email,
        @NotBlank(message = "Username must not be blank")
        String username,

        @NotBlank(message = "Password field must not be blank")
        @Size(min = 8, max = 20, message = "Password must be within 8 to 20 characters")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,20}$",
        message = "Password must contain at least one digit, one lowercase letter, one uppercase character and no whitespace")
        String password,

        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name cannot exceed 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name cannot exceed 50 characters")
        String lastName,

        @NotBlank(message = "National ID is required" )
        @Size(min = 5, max = 20)
        String nationalId,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone number must be valid") // Basic phone number validation
        String phoneNumber,
        // Addresses
        @Valid
        AddressDTO addressDTO

) {
}
