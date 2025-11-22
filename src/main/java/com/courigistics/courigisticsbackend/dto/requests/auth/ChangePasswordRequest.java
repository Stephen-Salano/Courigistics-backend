package com.courigistics.courigisticsbackend.dto.requests.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,
        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 20, message = "Password must be between 8 to 20 characters long")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\\\S+$).{8,20}$")
        String newPassword,
        @NotBlank(message = "Confirm password is required")
        String confirmPassword
) {
}
