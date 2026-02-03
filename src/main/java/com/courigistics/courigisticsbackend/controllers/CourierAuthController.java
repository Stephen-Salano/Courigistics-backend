package com.courigistics.courigisticsbackend.controllers;

import com.courigistics.courigisticsbackend.dto.requests.auth.LoginRequest;
import com.courigistics.courigisticsbackend.dto.requests.courier.CourierRegisterRequest;
import com.courigistics.courigisticsbackend.dto.requests.courier.CourierSetupAccountRequest;
import com.courigistics.courigisticsbackend.dto.responses.auth.AuthResponse;
import com.courigistics.courigisticsbackend.dto.responses.courier.CourierRegistrationResponse;
import com.courigistics.courigisticsbackend.services.auth.CourierAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class CourierAuthController {

    private final CourierAuthService courierAuthService;

    @PostMapping("/register/courier")
    public ResponseEntity<Map<String, Object>> registerCourier(
            @Valid @RequestBody CourierRegisterRequest request
            ){
        log.info("Courier registration attempt: {}", request.email());

        try{
            CourierRegistrationResponse response = courierAuthService.registerCourier(request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", response.message(),
                            "email", response.nextStep()
                    ));
        }catch (Exception e){
            log.error("Courier registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    @GetMapping("/verify/courier")
    public ResponseEntity<Map<String, Object>> verifyCourierEmail(
            @RequestParam("token") String token
    ){
        try{
            boolean verified = courierAuthService.verifyEmail(token);

            if (verified){
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Email verified! Your application is pending admin approval"
                ));
            }else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "success", false,
                                "message", "Invalid or expired verification token"
                        ));
            }
        }catch (Exception e) {
            log.error("Verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    @PostMapping("/setup-account/courier")
    public ResponseEntity<Map<String, Object>> setupAccount(
            @Valid @RequestBody CourierSetupAccountRequest request
            ){
        log.info("Account setup attempt for employee ID: {}", request.employeeId());

        try{
            courierAuthService.setupAccount(request);

            return ResponseEntity.ok(Map.of(
                    "succees", true,
                    "message", "Account setup complete! You can now log in.",
                    "username", request.username()
            ));
        }catch (Exception e){
            log.error("Account setup Failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    @PostMapping("/login/courier")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request){
        log.info("Courier login attempt: {}", request.usernameOrEmail());

        try{
            AuthResponse authResponse = courierAuthService.login(request);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Login successful",
                    "data", authResponse
            ));
        } catch (Exception e){
            log.error("Courier login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

}
