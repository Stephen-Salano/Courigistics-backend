package com.courigistics.courigisticsbackend.services.auth;

import com.courigistics.courigisticsbackend.dto.requests.auth.LoginRequest;
import com.courigistics.courigisticsbackend.dto.responses.auth.AuthResponse;
import org.springframework.security.core.Authentication;

public interface AdminAuthService {
    AuthResponse login(LoginRequest request);
    void logout(Authentication authentication);
}
