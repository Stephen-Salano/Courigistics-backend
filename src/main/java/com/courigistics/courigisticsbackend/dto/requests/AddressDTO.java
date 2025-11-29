package com.courigistics.courigisticsbackend.dto.requests;

import jakarta.validation.constraints.NotBlank;

public record AddressDTO(
        String label,
        @NotBlank(message = "Must have one address")
        String addressLine1,
        String addressLine2,
        @NotBlank(message = "City or town must be entered")
        String city,
        @NotBlank(message = "postal code must be entered")
        String postalCode,
        @NotBlank(message = "Country must be entered")
        String country,
        boolean isDefault
) {
}
