package com.courigistics.courigisticsbackend.controllers;

import com.courigistics.courigisticsbackend.dto.responses.customer.CustomerProfileResponse;
import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.services.customer.CustomerService;
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
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> getCustomerProfile(Authentication authentication) {
        Account account = (Account) authentication.getPrincipal();
        log.info("Fetching profile for customer: {}", account.getUsername());

        try {
            CustomerProfileResponse profile = customerService.getCustomerProfile(account.getId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", profile
            ));
        } catch (Exception e) {
            log.error("Failed to fetch customer profile for user {}: {}", account.getUsername(), e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Failed to fetch profile: " + e.getMessage()
            ));
        }
    }
}
