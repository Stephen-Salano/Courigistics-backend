package com.courigistics.courigisticsbackend.dto.requests.common;

import jakarta.validation.constraints.NotBlank;
import org.springframework.lang.Nullable;

public record AddressDTO(
        String label,
        @NotBlank(message = "Must have one address")
        String addressLine1,
        String addressLine2,
        @NotBlank(message = "City or town must be entered")
        String city,
        @Nullable
        String postalCode,
        @NotBlank(message = "Country must be entered")
        String country,
        boolean isDefault
) {
}
