package com.courigistics.courigisticsbackend.entities;

import com.courigistics.courigisticsbackend.utils.GeoUtils;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "addresses", indexes = {
        @Index(name = "idx_address_account", columnList = "account_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    private String label;

    @Column(name = "address_line1", nullable = false)
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "postal_code", nullable = true)
    private String postalCode;

    @Column(name = "country", nullable = false)
    private String country;

    /*
    Longitude coordinate for display and input
    Kept as Double for compatibility with frontend and existing code
     */
    private Double latitude;

    /*
    Longitude coordinate for display and input
    Kept as Double for compatibility with frontend and existing code
     */
    private Double longitude;

    /**
     * This is the PostGIS geography point for spatia queries
     *
     * it uses geography type (not geometry) for accurate distance calculations
     * on Earth's surface. Automatically populated from latitude/longitude
     * in @PrePersist and @PreUpdate
     *
     * Nullable to support H2 test database which doesn't have PostGIS
     * In tests, spatial queries fall back to haversine distance in Java
     */
    @Column(name = "location", columnDefinition = "geography(Point, 4326)", nullable = true)
    private Point location;

    @Column(name = "is_default")
    private boolean isDefault = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        populateLocationFromCoordinates();
    }

    @PreUpdate
    protected void onUpdate(){
        this.updatedAt = LocalDateTime.now();
        populateLocationFromCoordinates();
    }

    /**
     * Populates the PostGIS location field from latitude/longitude
     *
     * If either coordinate is null, location is set to null to maintain consistency.
     * This method is private and only called by lifecycle hooks to ensure
     * location is always in sync with lat/lng coordinates.
     */
    private void populateLocationFromCoordinates() {
        this.location = GeoUtils.createPointSafe(this.latitude, this.longitude);
    }

}
