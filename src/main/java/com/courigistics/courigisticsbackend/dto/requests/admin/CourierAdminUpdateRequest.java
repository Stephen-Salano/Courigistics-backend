package com.courigistics.courigisticsbackend.dto.requests.admin;

import com.courigistics.courigisticsbackend.entities.enums.CourierStatus;
import com.courigistics.courigisticsbackend.entities.enums.EmploymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CourierAdminUpdateRequest(
    @NotBlank(message = "First name is required")
    String firstName,

    @NotBlank(message = "Last name is required")
    String lastName,

    @NotNull(message = "Status is required")
    CourierStatus status,

    @NotNull(message = "Employment type is required")
    EmploymentType employmentType,

    UUID depotId,

    boolean availableForAssignment
) {}
