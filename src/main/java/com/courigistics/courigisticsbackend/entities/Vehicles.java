package com.courigistics.courigisticsbackend.entities;

import com.courigistics.courigisticsbackend.entities.enums.VehicleStatus;
import com.courigistics.courigisticsbackend.entities.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Entity
@Table(name = "vehicles", indexes = {
        @Index(name = "idx_vehicle_courier", columnList = "courier_id"),
        @Index(name = "idx_depot_id", columnList = "depot_id"),
        @Index(name = "idx_license_plate", columnList = "license_plate")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Vehicles {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "courier_id", nullable = false, unique = true)
    @OneToOne(fetch = FetchType.LAZY)
    private Courier courier;

    @JoinColumn(name = "depot_id", nullable = true)
    @ManyToOne(fetch = FetchType.LAZY)
    private Depot depot;

    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    @Column(name = "make", nullable = false)
    private String make;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "production_year")
    private String manufactureYear;

    @Column(name = "color")
    private String vehicleColor;

    @Column(name = "license_plate", unique = true)
    private String licencePlate;

    @Column(name = "chassis_number", unique = true)
    private String chassisNumber;

    @Column(name = "capacity_kg", nullable = false)
    private Double vehicleCapacityKg;

    @Column(name = "capacity_m3", nullable = false)
    private Double vehicleCapacityM3;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private VehicleStatus status = VehicleStatus.ACTIVE;

    @Column(name = "insurance_expiry_date")
    private String insuranceExpiryDate;


    // TODO : Add vehicle insurance number

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
