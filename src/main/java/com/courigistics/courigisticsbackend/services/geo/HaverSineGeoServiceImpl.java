package com.courigistics.courigisticsbackend.services.geo;

import com.courigistics.courigisticsbackend.entities.Courier;
import com.courigistics.courigisticsbackend.entities.Depot;
import com.courigistics.courigisticsbackend.entities.enums.CourierStatus;
import com.courigistics.courigisticsbackend.entities.enums.DepotStatus;
import com.courigistics.courigisticsbackend.entities.enums.EmploymentType;
import com.courigistics.courigisticsbackend.repositories.CourierRepository;
import com.courigistics.courigisticsbackend.repositories.DepotRepository;
import com.courigistics.courigisticsbackend.utils.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Haversine-based GeoService — active on the 'test' profile only.
 *
 * Uses pure Java distance calculations so tests can run against H2
 * without needing PostGIS installed.
 */

@Service
@Profile("test")
@RequiredArgsConstructor
@Slf4j
public class HaverSineGeoServiceImpl implements GeoService{

    private static  final double FREELANCER_RADIUS_KM = 10.0;

    private final DepotRepository depotRepository;
    private CourierRepository courierRepository;

    @Override
    public Optional<Depot> findNearestDepot(double latitude, double longitude) {
        log.debug("[Haversine] Finding nearest depot for ({}, {})", latitude, longitude);

        return depotRepository.findByStatus(DepotStatus.ACTIVE)
                .stream()
                .filter(depot -> depot.getLatitude() != null && depot.getLongitude() != null)
                .filter(depot -> {
                    double distance = GeoUtils.haversineDistance(
                            latitude, longitude, depot.getLatitude(), depot.getLongitude()
                    );
                    return distance <= depot.getCoverageRadiusKm();
                })
                .min(Comparator.comparingDouble(depot -> GeoUtils.haversineDistance(
                        latitude, longitude,
                        depot.getLatitude(), depot.getLongitude()
                )));
    }

    @Override
    public boolean isWithinCoverage(double latitude, double longitude) {
        return findNearestDepot(latitude, longitude).isPresent();
    }

    @Override
    public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        return GeoUtils.haversineDistance(lat1, lon1, lat2, lon2);
    }

    @Override
    public List<Courier> findNearbyFreelancers(double latitude, double longitude, String city) {
        log.debug("[Haversine] Finding freelancers near ({}, {}) in {}", latitude, longitude, city);

        return courierRepository.findByStatus(CourierStatus.ACTIVE)
                .stream()
                .filter(c -> c.getEmploymentType() == EmploymentType.FREELANCER)
                .filter(c -> Boolean.TRUE.equals(c.getAvailableForAssignment()))
                .filter(c -> city.equalsIgnoreCase(c.getOperationalCity()))
                .filter(c -> c.getCurrentLat() != null && c.getCurrentLon() != null)
                .filter(c -> {
                    double distance = GeoUtils.haversineDistance(
                            latitude, longitude,
                            c.getCurrentLat(), c.getCurrentLon()
                    );
                    return distance <= FREELANCER_RADIUS_KM;
                })
                .sorted(Comparator.comparingDouble(c -> GeoUtils.haversineDistance(
                        latitude, longitude,
                        c.getCurrentLat(), c.getCurrentLon()
                )))
                .toList();
    }
}
