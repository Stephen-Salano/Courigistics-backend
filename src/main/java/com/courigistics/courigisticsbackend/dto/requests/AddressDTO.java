package com.courigistics.courigisticsbackend.dto.requests;

public record AddressDTO(
        String label,
        String addressLine1,
        String addressLine2,
        String city,
        String postalCode,
        String country,
        boolean isDefault
) {
}
