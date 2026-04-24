package com.courigistics.courigisticsbackend.controllers;

import com.courigistics.courigisticsbackend.dto.requests.admin.DepotRequest;
import com.courigistics.courigisticsbackend.dto.responses.admin.DepotResponse;
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
@RequestMapping("/api/v1/admin/manage/depots")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminDepotController {

    private final AdminService adminService;
    private static final String SUCCESS = "success";
    private static final String DATA = "data";

    @PostMapping
    public ResponseEntity<Map<String, Object>> createDepot(@Valid @RequestBody DepotRequest request) {
        DepotResponse response = adminService.createDepot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(SUCCESS, true, DATA, response));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllDepots() {
        List<DepotResponse> depots = adminService.getAllDepots();
        return ResponseEntity.ok(Map.of(SUCCESS, true, DATA, depots));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDepotById(@PathVariable UUID id) {
        DepotResponse depot = adminService.getDepotById(id);
        return ResponseEntity.ok(Map.of(SUCCESS, true, DATA, depot));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateDepot(@PathVariable UUID id, @Valid @RequestBody DepotRequest request) {
        DepotResponse response = adminService.updateDepot(id, request);
        return ResponseEntity.ok(Map.of(SUCCESS, true, DATA, response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteDepot(@PathVariable UUID id) {
        adminService.deleteDepot(id);
        return ResponseEntity.ok(Map.of(SUCCESS, true, "message", "Depot removed successfully"));
    }
}
