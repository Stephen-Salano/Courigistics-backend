-- Production seed data for Courigistics Backend
-- Uses PostGIS geography types for spatial queries
-- Run after migrations complete

-- ============================================================================
-- DEPOTS - Major distribution centers in Kenya
-- ============================================================================
-- Note: ST_MakePoint takes (longitude, latitude) - X,Y order, not lat,lng
-- coverage_radius_km determines service area using ST_DWithin queries

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
    location,
    coverage_radius_km,
    status,
    created_at
) VALUES
-- Nairobi Main Depot - Serves entire Nairobi metro area
(
    gen_random_uuid(),
    'Nairobi Main Distribution Center',
    'NBO-MAIN',
    'STANDALONE',
    'Industrial Area, Nairobi',
    'Nairobi',
    'Kenya',
    -1.286389,
    36.817223,
    ST_MakePoint(36.817223, -1.286389)::geography,  -- (longitude, latitude)
    50.0,  -- 50km radius covers Nairobi, Kiambu, parts of Machakos
    'ACTIVE',
    NOW()
),

-- Mombasa Main Depot - Serves coastal region
(
    gen_random_uuid(),
    'Mombasa Main Distribution Center',
    'MBA-MAIN',
    'STANDALONE',
    'Port Reitz, Mombasa',
    'Mombasa',
    'Kenya',
    -4.043477,
    39.668206,
    ST_MakePoint(39.668206, -4.043477)::geography,  -- (longitude, latitude)
    30.0,  -- 30km radius covers Mombasa Island, Likoni, parts of Kilifi
    'ACTIVE',
    NOW()
),

-- Kisumu Main Depot - Serves western Kenya
(
    gen_random_uuid(),
    'Kisumu Main Distribution Center',
    'KSM-MAIN',
    'STANDALONE',
    'Kisumu CBD, Kisumu',
    'Kisumu',
    'Kenya',
    -0.091702,
    34.767956,
    ST_MakePoint(34.767956, -0.091702)::geography,  -- (longitude, latitude)
    25.0,  -- 25km radius covers Kisumu city and surroundings
    'ACTIVE',
    NOW()
);

-- ============================================================================
-- VERIFICATION QUERY (for testing in psql)
-- ============================================================================
-- Run this to verify depots are correctly placed:
--
-- SELECT
--     code,
--     name,
--     city,
--     coverage_radius_km,
--     ST_AsText(location) as location_wkt,
--     ST_Distance(
--         location,
--         ST_MakePoint(36.817223, -1.286389)::geography
--     ) / 1000 as distance_from_nairobi_km
-- FROM depot
-- ORDER BY code;
--
-- Expected output:
-- KSM-MAIN: ~290km from Nairobi
-- MBA-MAIN: ~435km from Nairobi
-- NBO-MAIN: 0km (itself)

-- ============================================================================
-- SAMPLE COVERAGE CHECK QUERY (for testing)
-- ============================================================================
-- Test if a point falls within any depot coverage:
--
-- -- Check if Westlands, Nairobi (-1.2674, 36.8078) is covered:
-- SELECT code, name
-- FROM depot
-- WHERE ST_DWithin(
--     location,
--     ST_MakePoint(36.8078, -1.2674)::geography,
--     coverage_radius_km * 1000
-- );
--
-- Expected: Returns NBO-MAIN (within 50km)