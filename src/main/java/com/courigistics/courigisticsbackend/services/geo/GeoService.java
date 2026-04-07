package com.courigistics.courigisticsbackend.services.geo;

import com.courigistics.courigisticsbackend.entities.Courier;
import com.courigistics.courigisticsbackend.entities.Depot;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction over spatial operations.
 *
 * Two implementations are profile-selected:
 * - HaversineGeoServiceImpl  → active on 'test' profile (H2, pure Java)
 * - PostGISGeoServiceImpl    → active on 'dev' and 'prod' profiles (PostgreSQL + PostGIS)
 *
 * No other class may run ST_DWithin / ST_Distance queries directly.
 * All spatial logic routes through here.
 */

public interface GeoService {
    /**
     * Finds the nearest active depot whose coverage radius includes the given point.
     * Returns empty if no depot covers the location.
     */
    Optional<Depot> findNearestDepot(double latitude, double longitude);

    /**
     * Returns true if any active depot covers the given point.
     */
    boolean isWithinCoverage(double latitude, double longitude);

    /**
     * Calculates straight-line distance in kilometres between two coordinates.
     */
    double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2);

    /**
     * Finds available FREELANCER couriers within 10km of the pickup point
     * whose vehicle can handle the required package category.
     *
     * Results are ordered by proximity (nearest first).
     */
    List<Courier> findNearbyFreelancers(double latitude, double longitude, String city);
}
