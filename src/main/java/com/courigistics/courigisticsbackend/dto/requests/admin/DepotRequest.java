package com.courigistics.courigisticsbackend.dto.requests.admin;

import com.courigistics.courigisticsbackend.entities.enums.DepotStatus;
import com.courigistics.courigisticsbackend.entities.enums.DepotType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DepotRequest(
    @NotBlank(message = "Name is required")
    String name,

    @NotBlank(message = "Code is required")
    String code,

    @NotBlank(message = "Address is required")
    String address,

    @NotBlank(message = "City is required")
    String city,

    @NotBlank(message = "Country is required")
    String country,

    @NotNull(message = "Latitude is required")
    Double latitude,

    @NotNull(message = "Longitude is required")
    Double longitude,

    @NotNull(message = "Coverage radius is required")
    Double coverageRadiusKm,

    @NotNull(message = "Status is required")
    DepotStatus status,

    @NotNull(message = "Depot type is required")
    DepotType depotType
) {}
