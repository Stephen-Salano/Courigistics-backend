package com.courigistics.courigisticsbackend.dto.requests.courier;

import com.courigistics.courigisticsbackend.dto.requests.vehicles.VehiclesDetailsDTO;
import com.courigistics.courigisticsbackend.entities.enums.EmploymentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * DTO for courier registration
 * Captures personal info, employment type, driver details, and optionally vehicle details
 */

public record CourierRegisterRequest(
        // === Personal Information (Step 1) ===
        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name cannot exceed 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name cannot exceed 50 characters")
        String lastName,

        @NotBlank(message = "National ID is required")
        @Size(min = 5, max = 20, message = "National ID must be between 5 and 20 characters")
        String nationalId,

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be valid")
        String phone,

        @NotNull(message = "Employment type is required")
        EmploymentType employmentType,

        // === Driver Details (Step 2) ===
        @NotBlank(message = "Driver's license number is required")
        String driversLicenseNumber,

        @NotNull(message = "License expiry date is required")
        @Future(message = "License expiry date must be in the future")
        LocalDate licenseExpiryDate,

        // === Vehicle Details (Step 2 - Only for FREELANCER) ===
        @Valid
        VehiclesDetailsDTO vehicleDetails
) {
}
