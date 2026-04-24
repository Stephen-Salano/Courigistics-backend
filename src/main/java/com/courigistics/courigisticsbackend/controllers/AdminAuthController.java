package com.courigistics.courigisticsbackend.controllers;

import com.courigistics.courigisticsbackend.dto.requests.auth.LoginRequest;
import com.courigistics.courigisticsbackend.dto.responses.auth.AuthResponse;
import com.courigistics.courigisticsbackend.services.auth.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login/admin")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Admin login request for: {}", request.usernameOrEmail());
        AuthResponse response = adminAuthService.login(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Login successful",
                "data", response
        ));
    }

    @PostMapping("/logout/admin")
    public ResponseEntity<Map<String, Object>> logout(Authentication authentication) {
        adminAuthService.logout(authentication);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logout successful"
        ));
    }
}
