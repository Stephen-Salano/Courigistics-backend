package com.courigistics.courigisticsbackend.dto.security;

/**
 * This is meant to hold :
 * @param fingerprintHash the fingerprint hash
 * @param userAgent the user agent string
 * @param ipAddress the IP address of the client
 */

public record SecurityContext(
        String fingerprintHash,
        String userAgent,
        String ipAddress,
        String browser,
        String os,
        String deviceType

) {
}
