package com.courigistics.courigisticsbackend.controllers;

import com.courigistics.courigisticsbackend.dto.responses.courier.CourierProfileResponse;
import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.services.courier.CourierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/courier")
@RequiredArgsConstructor
@Slf4j
public class CourierController {

    private final CourierService courierService;

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('COURIER')")
    public ResponseEntity<Map<String, Object>> getCourierProfile(Authentication authentication) {
        Account account = (Account) authentication.getPrincipal();
        log.info("Fetching profile for courier: {}", account.getUsername());

        try {
            CourierProfileResponse profile = courierService.getCourierProfile(account.getId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", profile
            ));
        } catch (Exception e) {
            log.error("Failed to fetch courier profile: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Failed to fetch profile: " + e.getMessage()
            ));
        }
    }
}
