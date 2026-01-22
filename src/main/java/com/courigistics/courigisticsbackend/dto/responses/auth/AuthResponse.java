package com.courigistics.courigisticsbackend.dto.responses.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        long expiresIn,

        String username,
        String email,
        String role
) {

    /**
     * Static factory method to create AuthResponse with Bearer token type
     * @param accessToken JWT access token
     * @param refreshToken JWT refresh token
     * @param expiresIn expiration time in seconds
     * @param username user's username
     * @param email user's email
     * @param role users role / permissions
     * @return AuthResponse with all the auth details
     */
    public static AuthResponse of(
            String accessToken, String refreshToken, long expiresIn,
            String username, String email, String role
    ){
        return new AuthResponse(
                accessToken, refreshToken, "Bearer ", expiresIn, username, email, role
        );
    }
}
