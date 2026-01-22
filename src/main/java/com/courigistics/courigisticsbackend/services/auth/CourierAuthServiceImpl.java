package com.courigistics.courigisticsbackend.services.auth;

import com.courigistics.courigisticsbackend.dto.requests.auth.LoginRequest;
import com.courigistics.courigisticsbackend.dto.requests.customer.CustomerRegisterRequest;
import com.courigistics.courigisticsbackend.dto.requests.auth.ResetPasswordRequest;
import com.courigistics.courigisticsbackend.dto.responses.auth.AuthResponse;
import com.courigistics.courigisticsbackend.entities.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("courierAuthService")
@Slf4j
@RequiredArgsConstructor
public class CourierAuthServiceImpl implements AuthService{

    @Override
    public Account registerAccount(CustomerRegisterRequest request) {
        return null;
    }

    @Override
    public boolean verifyEmail(String token) {
        return false;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        return null;
    }

    @Override
    public void logout(Authentication authentication) {

    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        return null;
    }

    @Override
    public void requestPasswordReset(String email) {

    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {

    }
}
