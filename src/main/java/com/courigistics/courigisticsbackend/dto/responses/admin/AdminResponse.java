package com.courigistics.courigisticsbackend.dto.responses.admin;

import java.util.UUID;

public record AdminResponse(
    UUID id,
    String firstName,
    String lastName,
    String email,
    String phone,
    String username,
    String employeeId,
    String department
) {}
