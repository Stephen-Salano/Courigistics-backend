package com.courigistics.courigisticsbackend.services.courier;

import com.courigistics.courigisticsbackend.dto.responses.courier.CourierProfileResponse;
import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.entities.Courier;
import com.courigistics.courigisticsbackend.entities.Vehicles;
import com.courigistics.courigisticsbackend.entities.enums.EmploymentType;
import com.courigistics.courigisticsbackend.exceptions.ResourceNotFoundException;
import com.courigistics.courigisticsbackend.repositories.AccountRepository;
import com.courigistics.courigisticsbackend.repositories.CourierRepository;
import com.courigistics.courigisticsbackend.repositories.VehicleRepository;
import com.courigistics.courigisticsbackend.utils.PhoneNumberUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourierServiceImpl implements CourierService {

    private final AccountRepository accountRepository;
    private final CourierRepository courierRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional(readOnly = true)
    public CourierProfileResponse getCourierProfile(UUID accountId) {
        log.info("Fetching courier profile for accountId: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Courier courier = courierRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Courier profile not found"));

        String depotName = courier.getDepot() != null ? courier.getDepot().getName() : null;
        String depotCode = courier.getDepot() != null ? courier.getDepot().getCode() : null;

        CourierProfileResponse.VehicleDTO vehicleDTO = null;

        // Only fetch vehicle for freelancers
        if (courier.getEmploymentType() == EmploymentType.FREELANCER) {
            Vehicles vehicle = vehicleRepository.findByCourier_id(courier.getId())
                    .orElse(null);

            if (vehicle != null) {
                vehicleDTO = new CourierProfileResponse.VehicleDTO(
                        vehicle.getVehicleType() != null ? vehicle.getVehicleType().name() : null,
                        vehicle.getMake(),
                        vehicle.getModel(),
                        vehicle.getLicencePlate(),
                        vehicle.getVehicleColor(),
                        vehicle.getVehicleCapacityKg(),
                        vehicle.getVehicleCapacityM3(),
                        vehicle.getStatus() != null ? vehicle.getStatus().name() : null
                );
            }
        }

        return new CourierProfileResponse(
                courier.getFirstName(),
                courier.getLastName(),
                account.getEmail(),
                PhoneNumberUtils.formatForDisplay(account.getPhone()),
                courier.getNationalId(),
                courier.getEmploymentType(),
                courier.getEmployeeId(),
                courier.getStatus(),
                depotName,
                depotCode,
                vehicleDTO
        );
    }
}
