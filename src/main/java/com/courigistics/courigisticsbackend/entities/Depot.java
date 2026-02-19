package com.courigistics.courigisticsbackend.entities;

import com.courigistics.courigisticsbackend.entities.enums.DepotStatus;
import com.courigistics.courigisticsbackend.entities.enums.DepotType;
import com.courigistics.courigisticsbackend.utils.GeoUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
/**
 * Depot entity representing physical locations for package handling and courier operations
 *
 * Uses PostGIS spatial features to:
 * - Determine coverage areas using radius-based queries
 * - Find nearest depots for delivery assignments
 * - Validate if addresses fall within depot service areas
 */
@Entity
@Table(name = "depot", indexes = {
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

    /**
     * Unique depot code (e.g., "NBO-MAIN", "MBA-WEST")
     * Used for depot assignment and routing logic
     */
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

    /*
    Latitude coordinate for display and geocoding
     */
    @Column(name = "latitude")
    private Double latitude;

    /*
    Longitude coordinate for display and geocoding
     */
    @Column(name = "longitude")
    private Double longitude;

    /**
     * PostGIS geography poit for spatial queries
     *
     * Enable efficient queries like:
     * - ST_DWithin(location, pickup_point, coverage_radius) - "Is this address within the depot coverage?"
     * - ST_Distance(location, delivery_point) - "What's the distance to this delivery?"
     *
     * Automatically synchronized with latitude/longitude via lifecycle hooks
     * Nullable for H2 test compatibility
     */
    @Column(name = "location", columnDefinition = "geography(Point, 4326)", nullable = true)
    private Point location;

    /*
    Service coverage radius in Kilometers
    Used with ST_DWithin to validate if addresses fall within this depot's service area
     */
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

    /**
     * Lifecycle hook: populate spatial field before persisting new depot
     */
    @PrePersist
    protected void onCreate(){
        if (this.createdAt == null){
            this.createdAt = LocalDateTime.now();
            populateLocationFromCoordinates();
        }
    }

    /**
     * Lifecycle hook: populate spatial field before persisting new depot
     */
    @PreUpdate
    protected void onUpdate(){
        populateLocationFromCoordinates();
    }

    private void populateLocationFromCoordinates() {
        this.location = GeoUtils.createPointSafe(this.latitude, this.longitude);
    }


}
