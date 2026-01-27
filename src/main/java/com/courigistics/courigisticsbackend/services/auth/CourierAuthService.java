package com.courigistics.courigisticsbackend.services.auth;

import com.courigistics.courigisticsbackend.dto.requests.auth.LoginRequest;
import com.courigistics.courigisticsbackend.dto.requests.courier.CourierRegisterRequest;
import com.courigistics.courigisticsbackend.dto.requests.courier.CourierSetupAccountRequest;
import com.courigistics.courigisticsbackend.dto.responses.auth.AuthResponse;
import com.courigistics.courigisticsbackend.dto.responses.courier.CourierRegistrationResponse;

import java.util.UUID;

public interface CourierAuthService {

    /**
     * Step 1: Register courier (creates account + courier + vehicle if freelancer)
     * Sends verification email
     */
    CourierRegistrationResponse registerCourier(CourierRegisterRequest request);

    /**
     * Step 2: Verify email
     * Reuses existing token validation
     */
    boolean verifyEmail(String token);

    /**
     * Step 3: [MOCKED] Admin approves courier
     * Generates employee ID and sends setup email
     * In real implementation, this would be called by AdminController
     */
    void approveCourier(UUID courierId, UUID adminId);

    /**
     * Step 4: Courier sets up account (username + password)
     * Uses employeeId as identifier
     */
    void setupAccount(CourierSetupAccountRequest request);

    /**
     * Step 5: Login
     * Reuses existing login logic
     */
    AuthResponse login(LoginRequest request);
}
