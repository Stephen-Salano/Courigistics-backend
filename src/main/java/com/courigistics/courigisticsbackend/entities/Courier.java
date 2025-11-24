package com.courigistics.courigisticsbackend.entities;

import com.courigistics.courigisticsbackend.entities.enums.CourierStatus;
import com.courigistics.courigisticsbackend.entities.enums.EmploymentType;
import com.courigistics.courigisticsbackend.entities.enums.PaymentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Getter
@Setter
@Table(name = "couriers", indexes = {
        @Index(name = "idx_courier_account", columnList = "account_id"),
        @Index(name = "idx_courier_depot", columnList = "depot_id"),
        @Index(name = "idx_courier_status", columnList = "status")
})
@NoArgsConstructor
@AllArgsConstructor
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
    @JoinColumn(name = "depot_id", nullable = false)
    private Depot depot;

    @Column(name = "employee_id", unique = true)
    private String employeeId;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    @Column(name = "national_id", unique = true)
    private String nationalId;

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

}
