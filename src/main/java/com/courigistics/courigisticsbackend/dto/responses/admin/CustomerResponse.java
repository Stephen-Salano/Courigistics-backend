package com.courigistics.courigisticsbackend.dto.responses.admin;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String username,
        Boolean enabled,
        Boolean emailVerified,
        LocalDateTime lastLogin,
        LocalDateTime createdAt
) {}
