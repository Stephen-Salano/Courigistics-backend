package com.courigistics.courigisticsbackend.dto.responses.admin;

import com.courigistics.courigisticsbackend.entities.enums.DepotStatus;
import com.courigistics.courigisticsbackend.entities.enums.DepotType;

import java.util.UUID;

public record DepotResponse(
    UUID id,
    String name,
    String code,
    String address,
    String city,
    String country,
    Double latitude,
    Double longitude,
    Double coverageRadiusKm,
    DepotStatus status,
    DepotType depotType
) {}
