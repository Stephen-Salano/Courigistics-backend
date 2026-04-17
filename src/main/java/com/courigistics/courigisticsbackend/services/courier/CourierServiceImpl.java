package com.courigistics.courigisticsbackend.services.courier;

import com.courigistics.courigisticsbackend.dto.responses.courier.CourierDashboardResponse;
import com.courigistics.courigisticsbackend.dto.responses.courier.CourierProfileResponse;
import com.courigistics.courigisticsbackend.dto.responses.delivery.DeliveryResponse;
import com.courigistics.courigisticsbackend.entities.Account;
import com.courigistics.courigisticsbackend.entities.Courier;
import com.courigistics.courigisticsbackend.entities.Delivery;
import com.courigistics.courigisticsbackend.entities.Vehicles;
import com.courigistics.courigisticsbackend.entities.enums.DeliveryStatus;
import com.courigistics.courigisticsbackend.entities.enums.EmploymentType;
import com.courigistics.courigisticsbackend.exceptions.ResourceNotFoundException;
import com.courigistics.courigisticsbackend.repositories.AccountRepository;
import com.courigistics.courigisticsbackend.repositories.CourierRepository;
import com.courigistics.courigisticsbackend.repositories.DeliveryRepository;
import com.courigistics.courigisticsbackend.repositories.VehicleRepository;
import com.courigistics.courigisticsbackend.utils.PhoneNumberUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourierServiceImpl implements CourierService {

    public static final String COURIER_PROFILE_NOT_FOUND = "Courier profile not found";
    private final AccountRepository accountRepository;
    private final CourierRepository courierRepository;
    private final VehicleRepository vehicleRepository;
    private final DeliveryRepository deliveryRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<DeliveryResponse> getCourierAssignments(UUID accountId, Pageable pageable) {
        log.info("Fetching assignments for courier with accountId: {}", accountId);
        Courier courier = courierRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(COURIER_PROFILE_NOT_FOUND));

        return deliveryRepository.findByCourier_Id(courier.getId(), pageable)
                .map(this::mapDeliveryToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CourierDashboardResponse getCourierDashboardStats(UUID accountId) {
        log.info("Fetching dashboard stats for courier with accountId: {}", accountId);
        Courier courier = courierRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(COURIER_PROFILE_NOT_FOUND));

        long completedToday = deliveryRepository.countByCourierAndStatusAndDate(
                courier.getId(), DeliveryStatus.DELIVERED, LocalDate.now());

        long active = deliveryRepository.findActiveByCourier_Id(courier.getId()).size();

        // For now, earnings and ratings are stubs
        return CourierDashboardResponse.builder()
                .completedToday(completedToday)
                .activeDeliveries(active)
                .pendingOffers(0) // Will be updated in Phase 4
                .totalEarningsToday(0.0)
                .averageRating(5.0)
                .build();
    }

    private DeliveryResponse mapDeliveryToResponse(Delivery delivery) {
        return DeliveryResponse.builder()
                .id(delivery.getId())
                .deliveryNumber(delivery.getDeliveryNumber())
                .trackingNumber(delivery.getPackages().getTrackingNumber())
                .status(delivery.getDeliveryStatus())
                .price(delivery.getEstimatedPrice())
                .routeType(delivery.getRouteType())
                .pickupAddress(delivery.getPickupAddress().getAddressLine1())
                .pickupLat(delivery.getPickupLat())
                .pickupLon(delivery.getPickupLon())
                .dropoffAddress(delivery.getDropoffAddress().getAddressLine1())
                .dropOffLat(delivery.getDropOffLat())
                .dropOffLon(delivery.getDropOffLon())
                .recipientName(delivery.getRecipientName())
                .recipientPhone(delivery.getRecipientPhone())
                .createdAt(delivery.getCreatedAt())
                .courierName(delivery.getCourier() != null ? delivery.getCourier().getFirstName() : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CourierProfileResponse getCourierProfile(UUID accountId) {
        log.info("Fetching courier profile for accountId: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Courier courier = courierRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(COURIER_PROFILE_NOT_FOUND));

        String depotName = courier.getDepot() != null ? courier.getDepot().getName() : null;
        String depotCode = courier.getDepot() != null ? courier.getDepot().getCode() : null;
        Double depotLat = courier.getDepot() != null ? courier.getDepot().getLatitude() : null;
        Double depotLon = courier.getDepot() != null ? courier.getDepot().getLongitude() : null;

        CourierProfileResponse.VehicleDTO vehicleDTO = null;

        // Only fetch vehicle for freelancers
        if (courier.getEmploymentType() == EmploymentType.FREELANCER) {
            Vehicles vehicle = vehicleRepository.findByCourier_id(courier.getId())
                    .orElse(null);

            if (vehicle != null) {
                vehicleDTO = CourierProfileResponse.VehicleDTO.builder()
                        .vehicleType(vehicle.getVehicleType() != null ? vehicle.getVehicleType().name() : null)
                        .make(vehicle.getMake())
                        .model(vehicle.getModel())
                        .licensePlate(vehicle.getLicencePlate())
                        .color(vehicle.getVehicleColor())
                        .capacityKg(vehicle.getVehicleCapacityKg())
                        .capacityM3(vehicle.getVehicleCapacityM3())
                        .status(vehicle.getStatus() != null ? vehicle.getStatus().name() : null)
                        .build();
            }
        }

        return CourierProfileResponse.builder()
                .firstName(courier.getFirstName())
                .lastName(courier.getLastName())
                .email(account.getEmail())
                .phone(PhoneNumberUtils.formatForDisplay(account.getPhone()))
                .nationalId(courier.getNationalId())
                .employmentType(courier.getEmploymentType())
                .employeeId(courier.getEmployeeId())
                .status(courier.getStatus())
                .depotName(depotName)
                .depotCode(depotCode)
                .depotLat(depotLat)
                .depotLon(depotLon)
                .currentLat(courier.getCurrentLat())
                .currentLon(courier.getCurrentLon())
                .vehicle(vehicleDTO)
                .build();
    }

    @Override
    @Transactional
    public void updateCourierLocation(UUID accountId, Double lat, Double lon) {
        log.info("Updating location for courier with accountId: {} to {},{}", accountId, lat, lon);
        Courier courier = courierRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(COURIER_PROFILE_NOT_FOUND));

        courier.setCurrentLat(lat);
        courier.setCurrentLon(lon);
        courier.setIsOnline(true);
        // Courier entity has @PreUpdate that calls populateCurrentLocationFromCoordinates()
        courierRepository.save(courier);
    }
}
