package com.courigistics.courigisticsbackend.services.validation;

public interface ValidationService {
    /**
     * Checks if a field value is already taken in the system.
     *
     * @param type  The type of field (email, username, phone, nationalId, licensePlate, driversLicense)
     * @param value The value to check
     * @return true if available (not taken), false if already exists
     */
    boolean isAvailable(String type, String value);
}
