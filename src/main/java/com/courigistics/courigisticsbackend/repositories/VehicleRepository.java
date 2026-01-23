package com.courigistics.courigisticsbackend.repositories;

import com.courigistics.courigisticsbackend.entities.Vehicles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicles, UUID> {

    Optional<Vehicles> findByCourier_id(UUID id);

    boolean existsByLicensePlate(String licensePlate);
}
