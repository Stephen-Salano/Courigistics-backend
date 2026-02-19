package com.courigistics.courigisticsbackend.utils;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Utility class for geospatial operations
 *
 * Provides methods for creating JTS Point objects for PostGIS storage
 * and calculating distances using the Haversine formula for H2 test compatibility.
 */
@RequiredArgsConstructor
public class GeoUtils {

    /**
     * SRID 4326 represents the WGS84 coordinate system used by GPS
     * This is the standard for latitude/longitude coordinates
     */
    private static final int WGS84_SRID = 4326;

    private static final double EARTH_RADIUS_KM = 6371.0;

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(
            new PrecisionModel(), WGS84_SRID
    );



    public static Point createPoint(double latitude, double longitude){
        ValidationUtils.validateCoordinates(latitude, longitude);

        // Our coordinate constructor takes (x, y) which is (longitude, latitude)
        Coordinate coordinate = new Coordinate(longitude, latitude);
        return GEOMETRY_FACTORY.createPoint(coordinate);
    }


    /**
     * Calculates the great-circle distance between two points using the Haversine formula
     *
     * This is a pure Java implementation used as a fallback for H2 database tests
     * where PostGIS ST_Distance is not available. In production with PostGIS,
     * prefer using ST_Distance for accuracy and performance.
     *
     * Formula: a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
     *          c = 2 ⋅ atan2(√a, √(1−a))
     *          d = R ⋅ c
     *
     * @param lat1 latitude of first point in degrees
     * @param lon1 longitude of first point in degrees
     * @param lat2 latitude of second point in degrees
     * @param lon2 longitude of second point in degrees
     * @return distance in kilometers
     * @throws IllegalArgumentException if any coordinates are out of valid range
     */
    public static double haversineDistance(double lat1, double lon1, double lat2, double lon2){
        ValidationUtils.validateCoordinates(lat1, lon1);
        ValidationUtils.validateCoordinates(lat2, lon2);

        // Convert degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Calculate differences
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Apply Haversine formula
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Helper method to safely create a Point from nullable coordinates
     * Returns null if either coordinate is null, preventing NullPointerException
     *
     * @param latitude  the latitude coordinate (can be null)
     * @param longitude the longitude coordinate (can be null)
     * @return a JTS Point or null if either coordinate is null
     */
    public static Point createPointSafe(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        return createPoint(latitude, longitude);
    }
}
