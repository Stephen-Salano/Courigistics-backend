package com.courigistics.courigisticsbackend.services.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService{
    @Value("${spring.application.frontend-url}")
    private String frontendUrl;

    @Override
    public void sendCustomerVerificationEmail(String to, String token) {
        String verifyUrl = frontendUrl + "/verify?token=" + token;
        log.info("Sending customer verification email to: {} with URL: {}", to, verifyUrl);

        // TODO: Implement with Thymeleaf template + JavaMailSender in Phase 7
        // For now, just log
    }

    @Override
    public void sendCourierVerificationEmail(String to, String token, String firstName) {
        String verifyUrl = frontendUrl + "/courier/verify?token=" + token;
        log.info("Sending courier verification email to: {} ({})", to, firstName);
        log.info("Verification URL: {}", verifyUrl);

        // TODO: Implement with template in Phase 7
    }

    @Override
    public void sendCourierPendingApprovalEmail(String to, String firstName) {
        log.info("Sending pending approval notification to: {} ({})", to, firstName);

        // TODO: Implement with template in Phase 7
    }

    @Override
    public void sendCourierApprovalEmail(String to, String firstName, String employeeId, String setUpToken) {
        String setupUrl = frontendUrl + "/courier/setup-account?token=" + setUpToken;
        log.info("Sending approval email to: {} ({})", to, firstName);
        log.info("Employee ID: {} | Setup URL: {}", employeeId, setupUrl);

        // TODO: Implement with template in Phase 7
    }

    @Override
    public void sendCourierAccountReadyEmail(String to, String firstName, String username) {
        log.info("Sending account ready email to: {} ({}) | Username: {}", to, firstName, username);

        // TODO: Implement with template in Phase 7
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        log.info("Sending password reset email to: {} with URL: {}", to, resetUrl);

        // TODO: Implement with template in Phase 7
    }
}
