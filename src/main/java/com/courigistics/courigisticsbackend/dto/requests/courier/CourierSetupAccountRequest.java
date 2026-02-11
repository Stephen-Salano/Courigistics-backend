package com.courigistics.courigisticsbackend.dto.requests.courier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for courier account setup.
 * - For Employees, 'token' is the Employee ID and 'username' is ignored.
 * - For Freelancers, 'token' is the setup token from the email and 'username' is required.
 */
public record CourierSetupAccountRequest(
        @NotBlank(message = "Identifier (Employee ID or Token) is required")
        String token,

        @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,20}$",
                message = "Password must contain at least one digit, one lowercase, one uppercase, one special character, and no whitespace"
        )
        String password,

        @NotBlank(message = "Confirm password is required")
        String confirmPassword
) {
}
