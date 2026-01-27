package com.courigistics.courigisticsbackend.utils;

import com.courigistics.courigisticsbackend.repositories.CourierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Year;

/**
 * Generates unique employee IDs for couriers
 * Format: COU-YYYY-XXXX (e.g., COU-2025-0001)
 */
@Component
@RequiredArgsConstructor
public class EmployeeIdGenerator {

    private final CourierRepository courierRepository;

    @Value("${app.courier.employee-id.prefix}")
    private String prefix;

    @Value("${app.courier.employee-id.sequence-length}")
    private int sequenceLength;

    public String generateEmployeeId(){
        int currentYear = Year.now().getValue();

        // count couriers created this year
        long count = courierRepository.countByCreatedAtYear(currentYear);

        // Next sequence number (1-based)
        long nextSequence = count + 1;

        // Format: COU-2025-0001
        String sequenceStr = String.format("%0" + sequenceLength + "d", nextSequence);

        return String.format("%s-%d-%s", prefix, currentYear, sequenceStr);
    }
}
