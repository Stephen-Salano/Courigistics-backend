package com.courigistics.courigisticsbackend.controllers;

import com.courigistics.courigisticsbackend.services.validation.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class ValidationController {

    public static final String SUCCESS = "success";
    private final ValidationService validationService;

    @GetMapping("/check-availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @RequestParam("type") String type,
            @RequestParam("value") String value
    ) {
        log.debug("Checking availability for type: {} and value: {}", type, value);
        
        try {
            boolean isAvailable = validationService.isAvailable(type, value);
            
            return ResponseEntity.ok(Map.of(
                    SUCCESS, true,
                    "available", isAvailable,
                    "type", type,
                    "value", value
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Validation check failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    SUCCESS, false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Unexpected error during validation check: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    SUCCESS, false,
                    "message", "An unexpected error occurred"
            ));
        }
    }
}
