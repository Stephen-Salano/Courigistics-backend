package com.courigistics.courigisticsbackend.controllers;

import com.courigistics.courigisticsbackend.dto.requests.delivery.DeliveryMilestoneRequest;
import com.courigistics.courigisticsbackend.dto.requests.delivery.DeliveryStatusUpdateRequest;
import com.courigistics.courigisticsbackend.dto.requests.delivery.LocationUpdateRequest;
import com.courigistics.courigisticsbackend.dto.responses.courier.CourierDashboardResponse;
import com.courigistics.courigisticsbackend.dto.responses.courier.CourierProfileResponse;
import com.courigistics.courigisticsbackend.dto.responses.delivery.DeliveryResponse;
import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.services.courier.CourierService;
import com.courigistics.courigisticsbackend.services.delivery.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courier")
@RequiredArgsConstructor
@Slf4j
public class CourierController {

    public static final String MESSAGE = "message";
    public static final String SUCCESS = "success";
    private final CourierService courierService;
    private final DeliveryService deliveryService;

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('COURIER')")
    public ResponseEntity<Map<String, Object>> getCourierProfile(Authentication authentication) {
        Account account = (Account) authentication.getPrincipal();
        log.info("Fetching profile for courier: {}", account.getUsername());

        CourierProfileResponse profile = courierService.getCourierProfile(account.getId());
        return ResponseEntity.ok(Map.of(SUCCESS, true, "data", profile));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('COURIER')")
    public ResponseEntity<Map<String, Object>> getDashboardStats(Authentication authentication) {
        Account account = (Account) authentication.getPrincipal();
        CourierDashboardResponse stats = courierService.getCourierDashboardStats(account.getId());
        return ResponseEntity.ok(Map.of(SUCCESS, true, "data", stats));
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasAuthority('COURIER')")
    public ResponseEntity<Page<DeliveryResponse>> getAssignedDeliveries(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Account account = (Account) authentication.getPrincipal();
        return ResponseEntity.ok(courierService.getCourierAssignments(account.getId(), pageable));
    }

    @PatchMapping("/delivery/{id}/status")
    @PreAuthorize("hasAuthority('COURIER')")
    public ResponseEntity<Map<String, Object>> updateDeliveryStatus(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody DeliveryStatusUpdateRequest request
    ) {
        deliveryService.updateDeliveryStatus(authentication, id, request.getStatus(), request.getNote());
        return ResponseEntity.ok(Map.of(
                SUCCESS, true,
                MESSAGE, "Delivery status updated to " + request.getStatus()
        ));
    }

    @PatchMapping("/location")
    @PreAuthorize("hasAuthority('COURIER')")
    public ResponseEntity<Map<String, Object>> updateLocation(
            Authentication authentication,
            @Valid @RequestBody LocationUpdateRequest request
    ) {
        Account account = (Account) authentication.getPrincipal();
        courierService.updateCourierLocation(account.getId(), request.lat(), request.lon());
        return ResponseEntity.ok(Map.of(SUCCESS, true, MESSAGE, "Location updated"));
    }

    @PostMapping("/delivery/{id}/milestone")
    @PreAuthorize("hasAuthority('COURIER')")
    public ResponseEntity<Map<String, Object>> updateMilestone(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody DeliveryMilestoneRequest request
    ) {
        deliveryService.updateDeliveryMilestone(authentication, id, request);
        return ResponseEntity.ok(Map.of(SUCCESS, true, MESSAGE, "Milestone updated"));
    }
}
