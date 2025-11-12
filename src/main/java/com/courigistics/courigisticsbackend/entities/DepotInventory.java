package com.courigistics.courigisticsbackend.entities;

import com.courigistics.courigisticsbackend.entities.enums.DepotInvenotryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "depot_inventory", indexes = {
        @Index(name = "idx_inventory_depot", columnList = "depot_id"),
        @Index(name = "idx_inventory_package", columnList = "package_id"),
        @Index(name = "idx_inventory_delivery", columnList = "delivery_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepotInventory {
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id", nullable = false)
    private Depot depot_id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", unique = true, nullable = false)
    private Packages packageEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @Column(name = "bay_location")
    private String bayLocation;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_courier")
    private Courier courier;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(name = "depot_invetory_status")
    @Enumerated(value = EnumType.STRING)
    private DepotInvenotryStatus depotInvenotryStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
