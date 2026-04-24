package com.courigistics.courigisticsbackend.dto.responses.courier;


import com.courigistics.courigisticsbackend.entities.enums.CourierStatus;
import com.courigistics.courigisticsbackend.entities.enums.EmploymentType;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CourierProfileResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String nationalId,
        EmploymentType employmentType,
        String employeeId,
        CourierStatus status,
        String depotName,
        String depotCode,
        Double depotLat,
        Double depotLon,
        Double currentLat,
        Double currentLon,
        Boolean availableForAssignment,
        VehicleDTO vehicle

) {
    @Builder
    public record VehicleDTO(
            String vehicleType,
            String make,
            String model,
            String licensePlate,
            String color,
            Double capacityKg,
            Double capacityM3,
            String status
    ){

    }
}
