package com.courigistics.courigisticsbackend.repositories;

import com.courigistics.courigisticsbackend.entities.Depot;
import com.courigistics.courigisticsbackend.entities.enums.DepotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepotRepository extends JpaRepository<Depot, UUID> {

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
}
