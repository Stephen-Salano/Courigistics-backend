package com.courigistics.courigisticsbackend.controllers;

import com.courigistics.courigisticsbackend.dto.responses.admin.AdminDashboardResponse;
import com.courigistics.courigisticsbackend.services.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    public static final String SUCCESS = "success";
    public static final String DATA = "data";
    private final AdminService adminService;

    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        AdminDashboardResponse stats = adminService.getDashboardStats();
        return ResponseEntity.ok(Map.of(SUCCESS, true, DATA, stats));
    }
}
