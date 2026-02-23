-- Test environment seed data for Courigistics Backend
-- H2 database compatible - no PostGIS syntax
-- The 'location' column is omitted as H2 doesn't support spatial types
-- Spatial operations in tests use GeoUtils.haversineDistance() instead

-- ============================================================================
-- DEPOTS - Test data for major Kenyan cities
-- ============================================================================
-- Note: 'location' column is NOT included - H2 will ignore it
-- Tests use latitude/longitude directly with Haversine calculations

INSERT INTO depot (
    id,
    name,
    code,
    depot_type,
    address,
    city,
    country,
    latitude,
    longitude,
    coverage_radius_km,
    status,
    created_at
) VALUES
-- Nairobi Main Depot
(
    RANDOM_UUID(),
    'Nairobi Main Distribution Center',
    'NBO-MAIN',
    'STANDALONE',
    'Industrial Area, Nairobi',
    'Nairobi',
    'Kenya',
    -1.286389,
    36.817223,
    50.0,
    'ACTIVE',
    CURRENT_TIMESTAMP
),

-- Mombasa Main Depot
(
    RANDOM_UUID(),
    'Mombasa Main Distribution Center',
    'MBA-MAIN',
    'STANDALONE',
    'Port Reitz, Mombasa',
    'Mombasa',
    'Kenya',
    -4.043477,
    39.668206,
    30.0,
    'ACTIVE',
    CURRENT_TIMESTAMP
),

-- Kisumu Main Depot
(
    RANDOM_UUID(),
    'Kisumu Main Distribution Center',
    'KSM-MAIN',
    'STANDALONE',
    'Kisumu CBD, Kisumu',
    'Kisumu',
    'Kenya',
    -0.091702,
    34.767956,
    25.0,
    'ACTIVE',
    CURRENT_TIMESTAMP
);

-- ============================================================================
-- TEST NOTES
-- ============================================================================
-- In test environment:
-- - Spatial queries are mocked or use GeoUtils.haversineDistance()
-- - Coverage checks compare distance against coverage_radius_km
-- - No ST_DWithin or ST_Distance - pure Java math instead
--
-- Example test logic:
--   double distance = GeoUtils.haversineDistance(
--       depotLat, depotLng,
--       pickupLat, pickupLng
--   );
--   boolean withinCoverage = distance <= depot.getCoverageRadiusKm();