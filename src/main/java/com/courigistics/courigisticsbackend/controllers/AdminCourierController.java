package com.courigistics.courigisticsbackend.controllers;

import com.courigistics.courigisticsbackend.dto.requests.admin.CourierAdminUpdateRequest;
import com.courigistics.courigisticsbackend.dto.responses.courier.CourierProfileResponse;
import com.courigistics.courigisticsbackend.entities.enums.CourierStatus;
import com.courigistics.courigisticsbackend.services.admin.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/manage/couriers")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminCourierController {

    private final AdminService adminService;
    private static final String SUCCESS = "success";
    private static final String DATA = "data";

    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingCouriers() {
        List<CourierProfileResponse> couriers = adminService.getPendingCouriers();
        return ResponseEntity.ok(Map.of(SUCCESS, true, DATA, couriers));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveCourier(@PathVariable UUID id) {
        adminService.approveCourier(id);
        return ResponseEntity.ok(Map.of(SUCCESS, true, "message", "Courier approved successfully"));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCouriers(Pageable pageable) {
        Page<CourierProfileResponse> couriers = adminService.getAllCouriers(pageable);
        return ResponseEntity.ok(Map.of(SUCCESS, true, DATA, couriers));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateCourierStatus(@PathVariable UUID id, @RequestParam CourierStatus status) {
        adminService.updateCourierStatus(id, status);
        return ResponseEntity.ok(Map.of(SUCCESS, true, "message", "Courier status updated to " + status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCourier(@PathVariable UUID id, @Valid @RequestBody CourierAdminUpdateRequest request) {
        adminService.updateCourier(id, request);
        return ResponseEntity.ok(Map.of(SUCCESS, true, "message", "Courier profile updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCourier(@PathVariable UUID id) {
        adminService.deleteCourier(id);
        return ResponseEntity.ok(Map.of(SUCCESS, true, "message", "Courier removed successfully"));
    }
}
