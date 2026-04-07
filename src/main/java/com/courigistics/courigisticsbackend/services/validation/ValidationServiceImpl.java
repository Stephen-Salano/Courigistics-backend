package com.courigistics.courigisticsbackend.services.validation;

import com.courigistics.courigisticsbackend.repositories.AccountRepository;
import com.courigistics.courigisticsbackend.repositories.CourierRepository;
import com.courigistics.courigisticsbackend.repositories.VehicleRepository;
import com.courigistics.courigisticsbackend.utils.PhoneNumberUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationServiceImpl implements ValidationService {

    private final AccountRepository accountRepository;
    private final CourierRepository courierRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    public boolean isAvailable(String type, String value) {
        if (value == null || value.isBlank()) return false;

        log.debug("Checking availability for type: {}, value: {}", type, value);

        return switch (type.toLowerCase()) {
            case "email" -> !accountRepository.existsByEmail(value);
            case "username" -> !accountRepository.existsByUsername(value);
            case "phone" -> !accountRepository.existsByPhone(PhoneNumberUtils.normalizePhoneNumber(value));
            case "nationalid" -> !courierRepository.existsByNationalId(value);
            case "licenseplate" -> !vehicleRepository.existsByLicencePlate(value);
            case "driverslicense" -> !courierRepository.existsByDriversLicenseNumber(value);
            default -> throw new IllegalArgumentException("Unknown validation type: " + type);
        };
    }
}
