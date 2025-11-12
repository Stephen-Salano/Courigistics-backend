package com.courigistics.courigisticsbackend.entities;

import com.courigistics.courigisticsbackend.entities.enums.EmploymentType;
import com.courigistics.courigisticsbackend.entities.enums.PaymentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", unique = true, nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id", nullable = false)
    private Depot depot;

    @Column(name = "employee_id", unique = true)
    private String employeeId;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

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

}
