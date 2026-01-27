package com.courigistics.courigisticsbackend.utils;

import com.courigistics.courigisticsbackend.dto.requests.vehicles.VehiclesDetailsDTO;
import com.courigistics.courigisticsbackend.entities.enums.EmploymentType;
import com.courigistics.courigisticsbackend.exceptions.BadRequestException;

import java.time.LocalDate;

/**
 * Validation utilities specific to courier registration and operations
 */
public class CourierValidationUtils {
    public static void validateVehicleRequired(EmploymentType employmentType, VehiclesDetailsDTO vehicle){
        if (employmentType == EmploymentType.FREELANCER && vehicle == null){
            throw new BadRequestException("Vehicle details are required for freelance couriers");
        }
    }

    public static void validateLicenseNotExpired(LocalDate expiryDate){
        if (expiryDate.isBefore(LocalDate.now())){
            throw new BadRequestException("Driver's license is expired");
        }
    }

    public static void validateCourierApproved(Boolean pendingApproval){
        if (Boolean.TRUE.equals(pendingApproval)){
            throw new BadRequestException("Account not yet approved by admin");
        }
    }
}
