package com.courigistics.courigisticsbackend.utils;

import ch.qos.logback.core.util.StringUtil;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class ValidationUtils {
    public static void validatePasswordsMatch(String password, String confirmPassword){
        Assert.hasText(password, "Passwords must not be empty");

        if(!password.equals(confirmPassword)){
            throw new IllegalArgumentException("Passwords do not match");
        }
    }
}
