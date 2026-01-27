package com.courigistics.courigisticsbackend.repositories;

import com.courigistics.courigisticsbackend.entities.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourierRepository extends JpaRepository<Courier, UUID> {


    Optional<Courier> findByEmployeeId(String employeeId);
    Optional<Courier> findByAccount_Email(String email);
    boolean existsByDriversLicenseNumber(String licenseNumber);
    List<Courier> findByPendingApprovalTrue();
    long countByCreatedAtYear(int year);

}
