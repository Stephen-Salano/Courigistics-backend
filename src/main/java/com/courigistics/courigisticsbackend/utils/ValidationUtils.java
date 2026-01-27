package com.courigistics.courigisticsbackend.utils;

import org.springframework.util.Assert;

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
}
