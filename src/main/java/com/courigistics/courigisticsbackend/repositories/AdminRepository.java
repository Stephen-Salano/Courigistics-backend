package com.courigistics.courigisticsbackend.repositories;

import com.courigistics.courigisticsbackend.entities.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID> {
    Optional<Admin> findByAccount_Email(String email);
    Optional<Admin> findByEmployeeId(String employeeId);
    boolean existsByEmployeeId(String employeeId);
}
