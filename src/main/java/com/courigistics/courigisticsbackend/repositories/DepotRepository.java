package com.courigistics.courigisticsbackend.repositories;

import com.courigistics.courigisticsbackend.entities.Depot;
import com.courigistics.courigisticsbackend.entities.enums.DepotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Depot entity with PostGIS spatial query support
 *
 * Enables coverage-based depot assignment and validation using geography distance calculations.
*/
@Repository
public interface DepotRepository extends JpaRepository<Depot, UUID> {


    /**
     * Finds the nearest depot that covers a given point
     *
     * Uses PostGIS ST_DWithin to check if point falls within depot's coverage radius.
     * Returns the closest depot among those that cover the point.
     *
     * Query explanation:
     * - ST_MakePoint creates a geography point from lng/lat (note: longitude first!)
     * - ST_DWithin checks if distance <= coverage_radius_km * 1000 (convert to meters)
     * - ST_Distance calculates actual distance for ordering
     * - LIMIT 1 returns only the closest matching depot
     *
     * Example: User at (-1.28, 36.81) in Nairobi
     * - NBO-MAIN at (-1.286389, 36.817223) with 50km radius → within coverage
     * - MBA-MAIN at (-4.043477, 39.668206) with 30km radius → too far
     * Returns NBO-MAIN
     *
     * @param latitude  the latitude of the point to check
     * @param longitude the longitude of the point to check
     * @return the nearest depot covering this point, or empty if no depot covers it
     */
    @Query(value = """
        SELECT d.* FROM depot d
        WHERE d.status = 'ACTIVE'
          AND ST_DWithin(
                d.location,
                ST_MakePoint(:longitude, :latitude)::geography,
                d.coverage_radius_km * 1000
              )
        ORDER BY ST_Distance(
                   d.location,
                   ST_MakePoint(:longitude, :latitude)::geography
                 )
        LIMIT 1
        """, nativeQuery = true)
    Optional<Depot> findNearestDepotWithinRadius(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude
    );

    /**
     * Finds all depots in a specific city
     * @param city the city the depot is within
     * @param status status of that depot
     * @return matching depot
     */
    List<Depot> findByCityAndStatus(String city, DepotStatus status);

    /**
     * Find depot by unique code (e.g., "NBO-MAIN", "MBA-WEST")
     * Used during courier registration to assign default depot
     */
    Optional<Depot> findByCode(String code);

    /**
     * Find all depots in a specific city
     * Useful for admin depot management and courier assignment
     */
    List<Depot> findByCity(String city);

    /**
     * Find all active depots
     * Used when assigning couriers or routing deliveries
     */
    List<Depot> findByStatus(DepotStatus status);

    /**
     * Find all sub-depots of a parent depot
     * Used for hierarchical depot management
     */
    List<Depot> findByParentDepot_Id(UUID parentDepotId);

    /**
     * Check if depot code already exists
     * Used during depot creation to prevent duplicates
     */
    boolean existsByCode(String code);

    @Query(value = """
    SELECT ST_Distance(
        ST_MakePoint(:lon1, :lat1)::geography,
        ST_MakePoint(:lon2, :lat2)::geography
    ) / 1000
    """, nativeQuery = true)
    double calculateDistanceBetweenPoints(
            @Param("lat1") double lat1,
            @Param("lon1") double lon1,
            @Param("lat2") double lat2,
            @Param("lon2") double lon2
    );
}
