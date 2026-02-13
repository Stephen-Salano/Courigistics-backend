package com.courigistics.courigisticsbackend.dto.responses.courier;


import com.courigistics.courigisticsbackend.entities.enums.CourierStatus;
import com.courigistics.courigisticsbackend.entities.enums.EmploymentType;

public record CourierProfileResponse(
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
        VehicleDTO vehicle

) {
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
