package com.courigistics.courigisticsbackend.entities;

import com.courigistics.courigisticsbackend.entities.enums.CourierStatus;
import com.courigistics.courigisticsbackend.entities.enums.EmploymentType;
import com.courigistics.courigisticsbackend.entities.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Getter
@Setter
@Table(name = "couriers", indexes = {
        @Index(name = "idx_courier_account", columnList = "account_id"),
        @Index(name = "idx_courier_depot", columnList = "depot_id"),
        @Index(name = "idx_courier_status", columnList = "status"),
        @Index(name = "idx_courier_employee_id", columnList = "employee_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Courier {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "profile_image", unique = true)
    private String profileImageUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", unique = true, nullable = false)
    private Account account;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CourierStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id", nullable = true)
    private Depot depot;

    @Column(name = "employee_id", unique = true)
    private String employeeId;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    @Column(name = "national_id", unique = true)
    private String nationalId;

    @Column(name = "drivers_license_number", unique = true, nullable = false)
    private String driversLicenseNumber;

    @Column(name = "license_expiry_date", nullable = false)
    private LocalDate licenseExpiryDate;

    @Column(name = "pending_approval", nullable = false)
    private Boolean pendingApproval = true;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Account approvedBy;

//    @Column(name = "vehicle_type")
//    @Enumerated(EnumType.STRING)
//    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Column(name = "base_salary")
    private BigDecimal baseSalary;

    @Column(name = "commission_rate")
    private BigDecimal commissionRate;

    @Column(name = "max_weight_per_route_kg")
    private Double maxWeightPerRoute = 100.0;

    @Column(name = "max_deliveries_per_route")
    private Integer maxDeliveriesPerDay = 20;

    @Column(name = "hired_at")
    private LocalDateTime hiredAt;

    @Column(name = "fired_at")
    private LocalDateTime firedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
