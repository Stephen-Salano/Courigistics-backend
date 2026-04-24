package com.courigistics.courigisticsbackend.controllers;

import com.courigistics.courigisticsbackend.dto.requests.admin.AdminCreateRequest;
import com.courigistics.courigisticsbackend.dto.requests.admin.AdminUpdateRequest;
import com.courigistics.courigisticsbackend.dto.responses.admin.AdminResponse;
import com.courigistics.courigisticsbackend.services.admin.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/manage/admins")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminManagementController {

    private final AdminService adminService;
    private static final String SUCCESS = "success";
    private static final String DATA = "data";

    @PostMapping
    public ResponseEntity<Map<String, Object>> createAdmin(@Valid @RequestBody AdminCreateRequest request) {
        AdminResponse response = adminService.createAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(SUCCESS, true, DATA, response));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAdmins() {
        List<AdminResponse> admins = adminService.getAllAdmins();
        return ResponseEntity.ok(Map.of(SUCCESS, true, DATA, admins));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAdminById(@PathVariable UUID id) {
        AdminResponse admin = adminService.getAdminById(id);
        return ResponseEntity.ok(Map.of(SUCCESS, true, DATA, admin));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAdmin(@PathVariable UUID id, @Valid @RequestBody AdminUpdateRequest request) {
        AdminResponse response = adminService.updateAdmin(id, request);
        return ResponseEntity.ok(Map.of(SUCCESS, true, DATA, response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAdmin(@PathVariable UUID id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.ok(Map.of(SUCCESS, true, "message", "Admin removed successfully"));
    }
}
