package com.courigistics.courigisticsbackend.controllers;

import com.courigistics.courigisticsbackend.dto.requests.admin.CustomerAdminUpdateRequest;
import com.courigistics.courigisticsbackend.dto.responses.admin.CustomerResponse;
import com.courigistics.courigisticsbackend.services.admin.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/manage/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminCustomerController {

    private final AdminService adminService;
    private static final String SUCCESS = "success";
    private static final String DATA = "data";

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCustomers(Pageable pageable) {
        Page<CustomerResponse> customers = adminService.getAllCustomers(pageable);
        return ResponseEntity.ok(Map.of(SUCCESS, true, DATA, customers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCustomerById(@PathVariable UUID id) {
        CustomerResponse customer = adminService.getCustomerById(id);
        return ResponseEntity.ok(Map.of(SUCCESS, true, DATA, customer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCustomer(@PathVariable UUID id, @Valid @RequestBody CustomerAdminUpdateRequest request) {
        CustomerResponse response = adminService.updateCustomer(id, request);
        return ResponseEntity.ok(Map.of(SUCCESS, true, DATA, response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCustomer(@PathVariable UUID id) {
        adminService.deleteCustomer(id);
        return ResponseEntity.ok(Map.of(SUCCESS, true, "message", "Customer removed successfully"));
    }
}
