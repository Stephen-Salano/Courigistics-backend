package com.courigistics.courigisticsbackend.utils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@AllArgsConstructor
@NoArgsConstructor
public class ValidationUtils {
    public static void validatePasswordsMatch(String password, String confirmPassword){
        /**
         * Validates that password and confirmPassword match
         * Used during registration and password reset
         */
        Assert.hasText(password, "Passwords must not be empty");
        Assert.hasText(confirmPassword, "Confirm password must not be empty");

        if(!password.equals(confirmPassword)){
            throw new IllegalArgumentException("Passwords do not match");
        }
    }

    /**
     * Validates that latitude and longitude are within valid ranges
     *
     * @param latitude  must be between -90 and 90 degrees
     * @param longitude must be between -180 and 180 degrees
     * @throws IllegalArgumentException if coordinates are invalid
     */
    public static void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException(
                    String.format("Latitude must be between -90 and 90, got: %.6f", latitude)
            );
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException(
                    String.format("Longitude must be between -180 and 180, got: %.6f", longitude)
            );
        }
    }
}
