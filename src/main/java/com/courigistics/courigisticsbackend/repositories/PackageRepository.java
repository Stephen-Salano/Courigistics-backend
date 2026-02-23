package com.courigistics.courigisticsbackend.repositories;

import com.courigistics.courigisticsbackend.entities.Packages;
import com.courigistics.courigisticsbackend.entities.enums.PackageCategory;
import com.courigistics.courigisticsbackend.entities.enums.PackageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Packages entity with tracking and lookup methods
 */
@Repository
public interface PackageRepository extends JpaRepository<Packages, UUID> {
    /**
     * Finds package by unique tracking number
     * Used for customer tracking and admin lookup
     *
     * Example tracking number: COU-PKG-20260223-0001
     *
     * @param trackingNumber the unique package identifier
     * @return package if found
     */
    Optional<Packages> findByTrackingNumber(String trackingNumber);

    /**
     * Checks if a tracking number already exists
     * Used during package creation to ensure uniqueness
     *
     * @param trackingNumber tracking number to check
     * @return true if tracking number exists
     */
    boolean existsByTrackingNumber(String trackingNumber);

    /**
     * Finds all packages sent by a customer
     * Used for customer package history
     *
     * @param accountId customer's account ID
     * @return list of packages
     */
    List<Packages> findBySenderAccount_Id(UUID accountId);

    /**
     * Finds packages by category
     * Used for analytics on package size distribution
     *
     * @param category package category (DOCUMENT, SMALL_PARCEL, etc.)
     * @return list of packages in that category
     */
    List<Packages> findByPackageCategory(PackageCategory category);

    /**
     * Finds packages by type
     * Used for filtering (e.g., all FRAGILE packages for special handling reports)
     *
     * @param packageType type of package (DOCUMENT, PARCEL, FRAGILE, EXTERNAL_ORDER)
     * @return list of packages of that type
     */
    List<Packages> findByPackageType(PackageType packageType);

    /**
     * Counts packages created in a date range
     * Used for daily/weekly/monthly statistics
     *
     * @param startDate start of date range
     * @param endDate   end of date range
     * @return count of packages created in range
     */
    @Query("""
        SELECT COUNT(p) FROM Packages p
        WHERE p.createdAt BETWEEN :startDate AND :endDate
        """)
    long countByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Finds all insured packages
     * Used for insurance tracking and reporting
     *
     * @return list of insured packages
     */
    List<Packages> findByIsInsuredTrue();

    /**
     * Finds all fragile packages
     * Used for special handling reports and courier briefings
     *
     * @return list of fragile packages
     */
    List<Packages> findByIsFragileTrue();

}
