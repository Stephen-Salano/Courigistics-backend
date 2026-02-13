package com.courigistics.courigisticsbackend.dto.responses.customer;

import java.util.List;
import java.util.UUID;

public record CustomerProfileResponse(
        String firstName,
        String lastName,
        String email,
        String phone,
        String profileImageUrl,
        List<AddressDTO> addresses
) {
    public record AddressDTO(
            UUID id,
            String label,
            String addressLine1,
            String addressLine2,
            String city,
            String postalCode,
            String country,
            boolean isDefault
    ) {}
}
