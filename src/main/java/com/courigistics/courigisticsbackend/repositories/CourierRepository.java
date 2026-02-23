package com.courigistics.courigisticsbackend.repositories;

import com.courigistics.courigisticsbackend.entities.Courier;
import com.courigistics.courigisticsbackend.entities.enums.CourierStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Courier entity with PostGIS proximity queries for assignment
 *
 * Enables Uber-style courier matching based on real-time location, availability,
 * and vehicle capacity.
 */
@Repository
public interface CourierRepository extends JpaRepository<Courier, UUID> {

    /**
     * Finds available freelance couriers near a pickup point
     *
     * This is the core query for Uber-style delivery assignment:
     * - Filters by operational city (quick pre-filter before expensive spatial query)
     * - Checks courier is ACTIVE and available for assignment
     * - Uses PostGIS ST_DWithin to find couriers within radius
     * - Orders by actual distance (nearest first)
     *
     * Vehicle capacity filtering happens in service layer:
     * vehicle.maxPackageCategory >= package.packageCategory
     *
     * Query flow:
     * 1. City filter: "Nairobi" → only Nairobi-based couriers
     * 2. Status: ACTIVE (not suspended/inactive)
     * 3. Employment: FREELANCER (employees use different logic)
     * 4. Availability: availableForAssignment = true (not on active delivery)
     * 5. Proximity: within radiusMeters of pickup point
     * 6. Order: by distance ascending (closest first)
     *
     * Example: Pickup in Westlands, Nairobi (-1.27, 36.80)
     * - Courier A: 2km away, ACTIVE, available, FREELANCER → included
     * - Courier B: 5km away, ACTIVE, but on delivery → excluded (not available)
     * - Courier C: 1km away, but in Mombasa → excluded (wrong city)
     * Returns [Courier C, Courier A] (sorted by distance)
     *
     * @param latitude     pickup location latitude
     * @param longitude    pickup location longitude
     * @param radiusMeters search radius in meters (e.g., 10000 = 10km)
     * @param city         operational city (e.g., "Nairobi")
     * @return list of available freelancers ordered by distance from pickup
     */
    @Query(value = """
        SELECT c.* FROM couriers c
        INNER JOIN vehicles v ON c.id = v.courier_id
        WHERE c.operational_city = :city
          AND c.status = 'ACTIVE'
          AND c.employment_type = 'FREELANCER'
          AND c.available_for_assignment = true
          AND c.current_location IS NOT NULL
          AND ST_DWithin(
                c.current_location,
                ST_MakePoint(:longitude, :latitude)::geography,
                :radiusMeters
              )
        ORDER BY ST_Distance(
                   c.current_location,
                   ST_MakePoint(:longitude, :latitude)::geography
                 )
        """, nativeQuery = true)
    List<Courier> findAvailableFreelancersNearPoint(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusMeters") double radiusMeters,
            @Param("city") String city
    );

    /**
     * Finds available employee couriers in a city
     *
     * Employees are depot-based so no proximity filter needed.
     * They serve the entire city/depot coverage area.
     *
     * Used as fallback if no nearby freelancers, or for intercity deliveries
     * where depot-to-depot transfers happen.
     *
     * @param city operational city
     * @return list of available employee couriers
     */
    @Query("""
        SELECT c FROM Courier c
        WHERE c.operationalCity = :city
          AND c.status = 'ACTIVE'
          AND c.employmentType = 'EMPLOYEE'
          AND c.availableForAssignment = true
        """)
    List<Courier> findAvailableEmployeesInCity(@Param("city") String city);

    /**
     * Finds courier by employee ID (for EMPLOYEE type only)
     */
    Optional<Courier> findByEmployeeId(String employeeId);

    /**
     * Finds all couriers in a specific depot
     * Used for depot-level courier management
     */
    @Query("""
        SELECT c FROM Courier c
        WHERE c.depot.id = :depotId
          AND c.status = :status
        """)
    List<Courier> findByDepotAndStatus(
            @Param("depotId") UUID depotId,
            @Param("status") CourierStatus status
    );



    Optional<Courier> findByAccount_Email(String email);
    Optional<Courier> findByAccount_Id(UUID accountId);
    boolean existsByDriversLicenseNumber(String licenseNumber);
    /**
     * Checks if a national ID is already registered
     * Used during courier onboarding validation
     */
    boolean existsByNationalId(String nationalId);
    List<Courier> findByPendingApprovalTrue();
    /**
     * Finds all couriers by status
     * Used for admin management and reporting
     */
    List<Courier> findByStatus(CourierStatus status);

    @Query("SELECT COUNT(c) FROM Courier c WHERE YEAR(c.createdAt) = :year")
    long countByCreatedAtYear(@Param("year") int year);

}
