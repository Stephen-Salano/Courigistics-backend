package com.courigistics.courigisticsbackend.dto.requests.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Token cannot be blank")
        String token,
        @NotBlank(message = "Password cannot be blank")
        @Size(min=8, message = "Password must be at least 8 characyers long")
        String newPassword,
        @NotBlank(message = "COnfirm password cannot be blank")
        String confirmPassword
) {
}
