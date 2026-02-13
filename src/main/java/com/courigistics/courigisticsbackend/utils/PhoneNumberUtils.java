package com.courigistics.courigisticsbackend.utils;

import com.courigistics.courigisticsbackend.exceptions.BadRequestException;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public class PhoneNumberUtils {

    private static final Pattern PLUS_PATTERN = Pattern.compile("^\\+");
    private static final Pattern DIGITS_ONLY = Pattern.compile("^[0-9]+$");

    /**
     * Normalizes phone number to format: countryCode + number (e.g., "254712345678")
     * - Strips leading '+' if present
     * - Validates result is digits only
     * - For Kenya: validates 12 digits (254 + 9 digits)
     *
     * @param rawPhone Raw phone input (e.g., "+254712345678" or "0712345678")
     * @return Normalized phone (e.g., "254712345678")
     * @throws BadRequestException if invalid format
     */
    public static String normalizePhoneNumber(String rawPhone){
        if (rawPhone == null || rawPhone.isBlank()){
            throw new BadRequestException("Phone number cannot be empty");
        }

        // Remove all spaces, dashes, parentheses
        String cleaned = rawPhone.replaceAll("[\\s\\-()]", "");

        // Strip leading '+'
        if (PLUS_PATTERN.matcher(cleaned).find()){
            cleaned = cleaned.substring(1);
        }

        // Handle local format: 0721345678 -> 254721345678
        if (cleaned.startsWith("0") && cleaned.length() == 10){
            cleaned = "254" + cleaned.substring(1);
            log.debug("Converted local format to international: {}", cleaned);
        }

        // Validate digits only
        if (!DIGITS_ONLY.matcher(cleaned).matches()){
            throw new BadRequestException("Phone number must contain only digits after normalization");
        }

        // Validate Kenya format (254 + 9 digits)
        if (cleaned.startsWith("254")){
            if (cleaned.length() != 12){
                throw new BadRequestException("Kenyan phone number must be 12 digits (254 + 9 digits)");
            }
        } else if (cleaned.length() < 10 || cleaned.length() > 15){
            throw new BadRequestException("Phone number must be between 10 & 15 digits");
        }

        log.debug("Normalized phone number: {}", cleaned);
        return cleaned;
    }

    /**
     * Formats normalized phone for display (adds '+')
     * @param normalized e.g., "254712345678"
     * @return e.g., "+254712345678"
     */
    public static String formatForDisplay(String normalized){
        if (normalized == null || normalized.isBlank()){
            return "";
        }
        return normalized.startsWith("+") ? normalized : "+" + normalized;
    }
}
