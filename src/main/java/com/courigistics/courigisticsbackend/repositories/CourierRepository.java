package com.courigistics.courigisticsbackend.repositories;

import com.courigistics.courigisticsbackend.entities.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourierRepository extends JpaRepository<Courier, UUID> {


    Optional<Courier> findByEmployeeId(String employeeId);
    Optional<Courier> findByAccount_Email(String email);
    Optional<Courier> findByAccount_Id(UUID accountId);
    boolean existsByDriversLicenseNumber(String licenseNumber);
    boolean existsByNationalId(String nationalId);
    List<Courier> findByPendingApprovalTrue();

    @Query("SELECT COUNT(c) FROM Courier c WHERE YEAR(c.createdAt) = :year")
    long countByCreatedAtYear(@Param("year") int year);

}
