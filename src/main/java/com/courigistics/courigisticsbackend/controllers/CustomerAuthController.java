package com.courigistics.courigisticsbackend.controllers;

import com.courigistics.courigisticsbackend.dto.requests.auth.RegisterRequest;
import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.services.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/auth/")
public class CustomerAuthController {

    // DI for the Auth service class
    @Qualifier("customerAuthService")
    @Autowired
    private AuthService authService;

    @PostMapping("/register/customer")
    public ResponseEntity<Map<String, Object>> registerCustomer(
            @Valid @RequestBody RegisterRequest registerCustomerRequest
            ){
        // log incoming registration attempt (without sensitive data)
        log.info("Registration attempt for username: {}", registerCustomerRequest.username());

        try{
            // Delegate the registration logic to our service layer
            Account registeredAccount = authService.registerAccount(registerCustomerRequest);

            // log successful registration
            log.info("User registered successfully: {}", registeredAccount.getUsername());

            // Return success response with helpful information
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", "Registration successful. Please check your email to verify your account",
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

    @GetMapping("/verify/customer")
    public ResponseEntity<Map<String, Object>> verifyCustomerEmail(@RequestParam("token")String token){
        // Log the verification attempt(token is logged for debugging)
        log.info("Email verification attempt with token:{}", token);

        try{

            // Delegate verification logic to out customer service layer
            boolean verificationSuccess = authService.verifyEmail(token);

            if (verificationSuccess){
                return ResponseEntity.ok(Map.of(
                        "success", "true",
                        "message", "Email verified successfully! Your account is now active and you can login",
                        "redirectUrl", "/login" // The frontend will use this to redirect the user
                ));
            } else{
                log.warn("Email Verification failed for token: {}", token);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "success", "false",
                                "message", "Invalid or expired verification token. Please request new verification token"
                        ));
            }
        } catch (Exception e){
            // Handling unexpected errors during email verification
            log.error("Unexpected error during email verification for token {}:{}", token, e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", "false",
                            "message", "Verification failed due to server error. Please try again later"
                    ));
        }
    }
}
