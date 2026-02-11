package com.courigistics.courigisticsbackend.controllers;

import com.courigistics.courigisticsbackend.dto.requests.auth.LoginRequest;
import com.courigistics.courigisticsbackend.dto.requests.auth.ForgotPasswordRequest;
import com.courigistics.courigisticsbackend.dto.requests.customer.CustomerRegisterRequest;
import com.courigistics.courigisticsbackend.dto.requests.auth.ResetPasswordRequest;
import com.courigistics.courigisticsbackend.dto.responses.auth.AuthResponse;
import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.services.auth.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @Value("${spring.application.frontend-url}")
    private String frontendUrl;

    @PostMapping("/register/customer")
    public ResponseEntity<Map<String, Object>> registerCustomer(
            @Valid @RequestBody CustomerRegisterRequest registerCustomerRequest
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
    public void verifyCustomerEmail(@RequestParam("token")String token, HttpServletResponse response) throws IOException {
        // Log the verification attempt(token is logged for debugging)
        log.info("Email verification attempt with token:{}", token);

        try{

            // Delegate verification logic to out customer service layer
            boolean verificationSuccess = authService.verifyEmail(token);

            if (verificationSuccess){
                response.sendRedirect(frontendUrl + "/verify/customer?success=true");
            } else{
                log.warn("Email Verification failed for token: {}", token);
                String message = URLEncoder.encode("Invalid or expired verification token.", StandardCharsets.UTF_8);
                response.sendRedirect(frontendUrl + "/verify/customer?success=false&message=" + message);
            }
        } catch (Exception e){
            // Handling unexpected errors during email verification
            log.error("Unexpected error during email verification for token {}:{}", token, e.getMessage(), e);
            String message = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect(frontendUrl + "/verify/customer?success=false&message=" + message);
        }
    }

    @PostMapping("/login/customer")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest loginRequest
            ){
        Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();

        // We check if a user is already authenticated and not an anonymous user
        // "anonymous" is he default principal for unauthenticated users in Spring Security
        if (currentAuthentication != null && currentAuthentication.isAuthenticated() &&
                !currentAuthentication.getPrincipal().equals("anonymousUser")){
            log.warn("Login attempt by already authenticated user: {}", currentAuthentication.getName());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "success", "false",
                            "message", "You are already logged in."
                            // TODO: Remember to introduce redirection on trying to login already
                    ));
        }
        log.info("Login attempt for: {}", loginRequest.usernameOrEmail());
        try{
            AuthResponse authResponse = authService.login(loginRequest);

            return ResponseEntity.ok(Map.of(
                    "success", "true",
                    "message", "Login successful",
                    "data", authResponse
            ));
        } catch (IllegalArgumentException e){
            log.warn("Login failed for {}: {}", loginRequest.usernameOrEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "success", "false",
                            "message", e.getMessage()
                    ));
        } catch (IllegalStateException e){
            log.warn("Account not verified for {}:{}", loginRequest.usernameOrEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "success", "false",
                            "message", e.getMessage()
                    ));
        }catch (Exception e){
            log.error("Unexpected error during login for {}:{}", loginRequest.usernameOrEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", "false",
                            "message", "Login failed due to server error. Please try again later"
                    ));
        }

    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(Authentication authentication){
        try{
            authService.logout(authentication);
            return ResponseEntity.ok(Map.of(
                    "success", "true",
                    "message", "logout successful"
            ));
        } catch (Exception e){
            log.error("Error during logout: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", "true",
                            "message", "logout failed due to a server error"
                    ));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(
            @RequestBody Map<String, String> requestBody
    ){
        String refreshToken = requestBody.get("refresh_token");

        if (refreshToken == null || refreshToken.trim().isEmpty()){
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", "false",
                            "message", "refresh token is required"
                    )
            );
        }

        try {
            AuthResponse authResponse = authService.refreshToken(refreshToken);

            return ResponseEntity.ok(
                    Map.of(
                            "success", "true",
                            "message", "Token refreshed successfully",
                            "data", authResponse
                    )
            );
        } catch (IllegalArgumentException e){
            log.warn("Token refresh failed {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "success", "false",
                            "message", e.getMessage()
                    ));

        }catch (Exception e){
            log.error("Unexpected error during token refresh: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", "false",
                            "message", "Token refresh failed due to server error"
                    ));
        }
    }

    @PostMapping("/forgot-password/customer")
    public ResponseEntity<Map<String, Object>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest
            ){
        log.info("Forgot password request received for email: {}", forgotPasswordRequest.email());
        authService.requestPasswordReset(forgotPasswordRequest.email());
        return ResponseEntity.ok(Map.of(
                "success", "true",
                "message", "If an account with that email exist, a password reset link has been sent"
        ));
    }

    @PostMapping("/reset-password/customer")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
            ){
        try{
            authService.resetPassword(request);
            return ResponseEntity.ok(
                    Map.of(
                            "success", "true",
                            "message", "Your password has been reset successfully. You can now login"
                    )
            );
        }catch (IllegalArgumentException e){
            log.warn("Password reset failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", "false",
                            "message", e.getMessage()
                    ));
        }catch (Exception e){
            log.warn("Unexpected server error durng password reset: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", "false",
                            "message", "Password reset failed due to error. Please try again later"
                    ));
        }
    }
}
