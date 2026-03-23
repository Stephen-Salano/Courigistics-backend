package com.courigistics.courigisticsbackend.dto.requests.delivery;

import com.courigistics.courigisticsbackend.entities.enums.PackageCategory;
import com.courigistics.courigisticsbackend.entities.enums.PackageType;
import com.courigistics.courigisticsbackend.entities.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDeliveryRequest(

        // === Pickup ===
        @NotNull(message = "Pickup latitude is required")
        Double pickupLat,

        @NotNull(message = "Pickup longitude is required")
        Double pickupLon,

        @NotBlank(message = "Pickup address line is required")
        String pickupAddressLine,

        @NotBlank(message = "Pickup city is required")
        String pickupCity,

        // === Dropoff ===
        @NotNull(message = "Dropoff latitude is required")
        Double dropOffLat,

        @NotNull(message = "Dropoff longitude is required")
        Double dropOffLon,

        @NotBlank(message = "Dropoff address line is required")
        String dropOffAddressLine,

        @NotBlank(message = "Dropoff city is required")
        String dropOffCity,

        // === Package ===
        @NotNull(message = "Package category is required")
        PackageCategory packageCategory,

        @NotNull(message = "Package type is required")
        PackageType packageType,

        @NotBlank(message = "Package description is required")
        String description,

        Boolean isFragile,

        Boolean isInsured,

        // === Recipient ===
        @NotNull(message = "Google Maps distance is required")
        Double googleMapsDistanceKm,

        @NotBlank(message = "Recipient name is required")
        String recipientName,

        @NotBlank(message = "Recipient phone is required")
        String recipientPhone,

        // === Payment ===
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        Boolean requiresSignature
) {
}
