package com.courigistics.courigisticsbackend.dto.requests.vehicles;

import com.courigistics.courigisticsbackend.entities.enums.VehicleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Vehicle information for freelance couriers
 * Only required when employmentType = FREELANCER
 */
public record VehiclesDetailsDTO(

        @NotNull(message = "Vehicle type is required")
        VehicleType vehicleType,

        @NotBlank(message = "Vehicle make is required")
        String make,

        @NotBlank(message = "Vehicle model is required")
        String model,

        @NotBlank(message = "License plate is required")
        String licensePlate,

        @NotNull(message = "Year is required")
        @Min(value = 1990, message = "Vehicle must be manufactured after 1990")
        Integer year,

        String color,

        @NotNull(message = "Vehicle capacity (kg) is required")
        @Min(value = 1, message = "Capacity must be at least 1 kg")
        Double capacityKg,

        @NotNull(message = "Vehicle capacity (m³) is required")
        @Min(value = 1, message = "Capacity must be at least 1 m³")
        Double capacityM3
) {
}
