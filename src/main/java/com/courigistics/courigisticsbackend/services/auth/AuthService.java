package com.courigistics.courigisticsbackend.services.auth;

import com.courigistics.courigisticsbackend.dto.requests.auth.LoginRequest;
import com.courigistics.courigisticsbackend.dto.requests.customer.CustomerRegisterRequest;
import com.courigistics.courigisticsbackend.dto.requests.auth.ResetPasswordRequest;
import com.courigistics.courigisticsbackend.dto.responses.auth.AuthResponse;
import com.courigistics.courigisticsbackend.entities.Account;
import org.springframework.security.core.Authentication;

/**
 * Service responsible for authentication-related operations including:
 *  - User registration
 *  - Verification
 *  - Login
 *  - Sign out
 */


public interface AuthService {

    /**
     * Registers a new user in the system
     * This processincludes:
     * 1. Validating the registration data
     * 2. Creating user and Account entities
     * 3. Generating a verification token
     * TODO: 4. Sending a verification email ()
     * @param request the registration request data provided by the user
     * @return the newly created Account entity
     * @throws IllegalArgumentException if the registration data is invalid
     * @throws RuntimeException if there is an error during registration
     */
    Account registerAccount(CustomerRegisterRequest request);



    /**
     * Verifies a users email using a token sent during registration
     *
     * @param token the verification token
     * @return true if verification was successful, false if otherwise
     * @throws IllegalArgumentException if the token is invalid
     */
    boolean verifyEmail(String token);

    /**
     * Authenticate a user and returns JWT tokens
     * @param request  the login credentials
     * @return AuthResponse containing access and refresh tokens
     */
    AuthResponse login(LoginRequest request);

    /**
     * Logs out a user by invalidating their refresh tokens
     * @param authentication the current user's authentication
     */
    void logout(Authentication authentication);

    /**
     * Refreshes an existing token using a valid refresh token
     * @param refreshToken the refresh token
     * @return AuthResponse with new tokens
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * Initiates the password reset process for user
     * Generates a password reset token and triggers an email to be sent
     * @param email the user's email address
     */
    void requestPasswordReset(String email);

    /**
     * Resets the user's password
     * @param request the password reset request containing the token and new password
     */
    void resetPassword(ResetPasswordRequest request);
}
