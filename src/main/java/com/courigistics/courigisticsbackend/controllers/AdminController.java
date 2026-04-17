package com.courigistics.courigisticsbackend.controllers;

import com.courigistics.courigisticsbackend.dto.responses.admin.AdminDashboardResponse;
import com.courigistics.courigisticsbackend.dto.responses.courier.CourierProfileResponse;
import com.courigistics.courigisticsbackend.services.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    public static final String SUCCESS = "success";
    private final AdminService adminService;

    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        AdminDashboardResponse stats = adminService.getDashboardStats();
        return ResponseEntity.ok(Map.of(SUCCESS, true, "data", stats));
    }

    @GetMapping("/couriers/pending")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPendingCouriers() {
        List<CourierProfileResponse> couriers = adminService.getPendingCouriers();
        return ResponseEntity.ok(Map.of(SUCCESS, true, "data", couriers));
    }

    @PostMapping("/couriers/{id}/approve")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> approveCourier(@PathVariable UUID id) {
        adminService.approveCourier(id);
        return ResponseEntity.ok(Map.of(SUCCESS, true, "message", "Courier approved successfully"));
    }
}
