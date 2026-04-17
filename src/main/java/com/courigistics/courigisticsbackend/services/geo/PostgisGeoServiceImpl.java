package com.courigistics.courigisticsbackend.services.geo;

import com.courigistics.courigisticsbackend.entities.Courier;
import com.courigistics.courigisticsbackend.entities.Depot;
import com.courigistics.courigisticsbackend.repositories.CourierRepository;
import com.courigistics.courigisticsbackend.repositories.DepotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "prod"})
public class PostgisGeoServiceImpl implements GeoService{

    private static final double FREELANCER_RADIUS_METERS = 10_000; // 10kilometers

    private final DepotRepository depotRepository;
    private final CourierRepository courierRepository;


    @Override
    public Optional<Depot> findNearestDepot(double latitude, double longitude) {
        log.debug("[PostGIS] Finding nearest depot for ({}, {})", latitude, longitude);

        return depotRepository.findNearestDepotWithinRadius(longitude, latitude);
    }

    @Override
    public boolean isWithinCoverage(double latitude, double longitude) {
        return depotRepository.findNearestDepotWithinRadius(longitude, latitude).isPresent();
    }

    @Override
    public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        log.debug("[PostGIS] Calculating ST_Distance between ({},{}) and ({},{})", lat1, lon1, lat2, lon2);
        return depotRepository.calculateDistanceBetweenPoints(lon1, lat1, lon2, lat2);
    }

    @Override
    public List<Courier> findNearbyFreelancers(double latitude, double longitude, String city) {
        log.debug("[PostGIS] Finding nearby freelancers for ({}, {}) in {}", latitude, longitude, city);
        return courierRepository.findAvailableFreelancersNearPoint(longitude, latitude, FREELANCER_RADIUS_METERS, city);
    }
}
