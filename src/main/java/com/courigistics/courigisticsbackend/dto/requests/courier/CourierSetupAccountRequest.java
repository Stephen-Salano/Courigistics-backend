package com.courigistics.courigisticsbackend.dto.requests.courier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for courier account setup (Step 5)
 * After admin approval, courier uses employeeId to set username and password
 */
public record CourierSetupAccountRequest(
        @NotBlank(message = "Employee ID is required")
        String employeeId,

        @NotBlank(message = "Username is required")
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
