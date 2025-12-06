package com.courigistics.courigisticsbackend.controllers;

import com.courigistics.courigisticsbackend.dto.requests.auth.RegisterRequest;
import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.services.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/auth/")
public class AuthController {

    // DI for the Auth service class
    private final AuthService authService;

    @PostMapping("/register/customer")
    public ResponseEntity<Map<String, Object>> registerCustomer(
            @Valid @RequestBody RegisterRequest registerCustomerRequest
            ){
        // log incoming registration attempt (without sensitive data)
        log.info("Registration attempt for username: {}", registerCustomerRequest.username());

        try{
            // Delegate the registration logic to our service layer
            Account registeredAccount = authService.registerCustomer(registerCustomerRequest);

            // log successful registration
            log.info("User registered successfully: {}", registeredAccount.getUsername());

            // Return success response with helpful information
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", "Registration successfull. Please check your email to verify your account",
                            "username", registeredAccount.getUsername(),
                            "email", registeredAccount.getEmail()
                    ));
        } catch (IllegalArgumentException e){
            // Handle validation errors (duplicate usernames/ emails)
            log.warn("Registration failed for: {} : {}", registerCustomerRequest.username(), e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        } catch (Exception e){
            // print the full stack trace of the root cause
            Throwable root = NestedExceptionUtils.getRootCause(e);
            if (root != null){
                log.error("Registration failed: root cause = {}", root.getMessage(), root);
            } else {
                log.error("Registration failed: {}", e.getMessage(), e);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", "false",
                            "message", "Registration failed due to a server error. Please try again later"
                    ));
        }

    }
}
