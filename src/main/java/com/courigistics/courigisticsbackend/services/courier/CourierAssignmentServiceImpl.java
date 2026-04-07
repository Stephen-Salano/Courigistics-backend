package com.courigistics.courigisticsbackend.services.courier;

import com.courigistics.courigisticsbackend.dto.responses.delivery.TierOptionResponse;
import com.courigistics.courigisticsbackend.entities.Courier;
import com.courigistics.courigisticsbackend.entities.Delivery;
import com.courigistics.courigisticsbackend.entities.Vehicles;
import com.courigistics.courigisticsbackend.entities.enums.PackageCategory;
import com.courigistics.courigisticsbackend.entities.enums.VehicleType;
import com.courigistics.courigisticsbackend.exceptions.ResourceNotFoundException;
import com.courigistics.courigisticsbackend.repositories.CourierRepository;
import com.courigistics.courigisticsbackend.repositories.VehicleRepository;
import com.courigistics.courigisticsbackend.services.geo.GeoService;
import com.courigistics.courigisticsbackend.utils.PriceCalculatorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourierAssignmentServiceImpl implements CourierAssignmentService {

    private final CourierRepository courierRepository;
    private final VehicleRepository vehicleRepository;
    private final GeoService geoService;

    @Override
    @Transactional(readOnly = true)
    public List<TierOptionResponse> getAvailableTiers(
            double pickupLat,
            double pickupLon,
            PackageCategory category,
            boolean isFragile,
            double distanceKm,
            String city
    ) {
        log.info("Calculating available tiers for package category: {} in {}", category, city);

        List<TierOptionResponse> availableTiers = new ArrayList<>();

        for (VehicleType tier : VehicleType.values()) {
            TierOptionResponse tierOption = processTier(pickupLat, pickupLon, category, isFragile, distanceKm, city, tier);
            if (tierOption != null) {
                availableTiers.add(tierOption);
            }
        }

        return availableTiers;
    }

    private TierOptionResponse processTier(double lat, double lon, PackageCategory category, boolean isFragile, double distanceKm, String city, VehicleType tier) {
        BigDecimal price = PriceCalculatorUtils.calculatePrice(tier, distanceKm, isFragile, category);
        List<Courier> candidates = findCandidates(lat, lon, category, city);
        
        List<TierOptionResponse.CourierSummary> summaries = new ArrayList<>();
        int count = 0;

        for (Courier courier : candidates) {
            if (isCourierEligible(courier, tier, category)) {
                count++;
                if (summaries.size() < 3) {
                    summaries.add(buildCourierSummary(lat, lon, courier));
                }
            }
        }

        return count > 0 ? new TierOptionResponse(tier, capitalize(tier.name()), price, count, summaries) : null;
    }

    private List<Courier> findCandidates(double lat, double lon, PackageCategory category, String city) {
        if (category == PackageCategory.SMALL || category == PackageCategory.MEDIUM) {
            List<Courier> freelancers = geoService.findNearbyFreelancers(lat, lon, city);
            return !freelancers.isEmpty() ? freelancers : courierRepository.findAvailableEmployeesInCity(city);
        }
        return courierRepository.findAvailableEmployeesInCity(city);
    }

    private boolean isCourierEligible(Courier courier, VehicleType tier, PackageCategory category) {
        return vehicleRepository.findByCourier_id(courier.getId())
                .map(v -> v.getVehicleType() == tier && canCarry(v, category))
                .orElse(false);
    }

    private TierOptionResponse.CourierSummary buildCourierSummary(double lat, double lon, Courier courier) {
        double dist = geoService.calculateDistanceKm(
                lat, lon,
                courier.getCurrentLat() != null ? courier.getCurrentLat() : 0,
                courier.getCurrentLon() != null ? courier.getCurrentLon() : 0
        );

        return new TierOptionResponse.CourierSummary(
                courier.getId(),
                courier.getFirstName(),
                5.0, // Mock rating for now
                dist
        );
    }

    @Override
    public Courier assignCourier(Delivery delivery, VehicleType tier, UUID courierId) {
        log.info("Assigning courier {} to delivery {}", courierId, delivery.getId());

        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("Courier not found with ID: " + courierId));

        if (Boolean.FALSE.equals(courier.getAvailableForAssignment())) {
            throw new IllegalStateException("Courier is no longer available for assignment");
        }

        // 1. Set courier unavailable
        courier.setAvailableForAssignment(false);

        // 2. Link courier to delivery and update status
        delivery.setCourier(courier);
        delivery.setDeliveryStatus(com.courigistics.courigisticsbackend.entities.enums.DeliveryStatus.ASSIGNED);
        delivery.setUpdatedAt(java.time.LocalDateTime.now());

        return courierRepository.save(courier);
    }

    private boolean canCarry(Vehicles vehicle, PackageCategory category) {
        return vehicle.getMaxPackageCategory().ordinal() >= category.ordinal();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
