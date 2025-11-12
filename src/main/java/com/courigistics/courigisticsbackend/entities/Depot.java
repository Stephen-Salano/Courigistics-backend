package com.courigistics.courigisticsbackend.entities;

import com.courigistics.courigisticsbackend.entities.enums.DepotStatus;
import com.courigistics.courigisticsbackend.entities.enums.DepotType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "depots", indexes = {
        @Index(name = "idx_depot_code", columnList = "code"),
        @Index(name = "idx_depot_city", columnList = "city"),
        @Index(name = "idx_depot_parent", columnList = "parent_depot_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Depot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    private DepotType depotType = DepotType.STANDALONE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_depot_id")
    private Depot parentDepot;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "coverage_radius_km")
    private Double coverageRadiusKm = 50.0;

    @Enumerated(EnumType.STRING)
    private DepotStatus status = DepotStatus.ACTIVE;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Relationships
    @OneToMany(mappedBy = "depot")
    private List<Courier> couriers;

    @OneToMany(mappedBy = "parentDepot")
    private List<Depot> subDepots;

}
