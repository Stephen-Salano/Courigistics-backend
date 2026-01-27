package com.courigistics.courigisticsbackend.repositories;

import com.courigistics.courigisticsbackend.entities.Vehicles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicles, UUID> {

    /**
     * Find vehicle by courier ID
     */
    Optional<Vehicles> findByCourier_id(UUID id);

    /**
     *Find Vehicle by courier ID
     */
    boolean existsByLicencePlate(String licencePlate);
}
