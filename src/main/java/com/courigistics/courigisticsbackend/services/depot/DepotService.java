package com.courigistics.courigisticsbackend.services.depot;

import com.courigistics.courigisticsbackend.entities.Depot;

public interface DepotService {

    /**
     * Finds the nearest active depot covering the given coordinates.
     * Throws BadRequestException if no depot covers the location.
     *
     * @param latitude  pickup or dropoff latitude
     * @param longitude pickup or dropoff longitude
     * @return the nearest covering depot
     */
    Depot findNearestDepotFor(double latitude, double longitude);

    /**
     * Finds the nearest active depot covering the given coordinates.
     * Returns empty if no depot covers the location.
     *
     * @param latitude  pickup or dropoff latitude
     * @param longitude pickup or dropoff longitude
     * @return optional containing the nearest covering depot
     */
    java.util.Optional<Depot> findOptionalNearestDepotFor(double latitude, double longitude);

    /**
     * Returns true if any active depot covers the given coordinates.
     * Used to validate pickup/dropoff addresses before creating a delivery.
     *
     * @param latitude  address latitude
     * @param longitude address longitude
     * @return true if location is serviceable
     */
    boolean isWithinCoverage(double latitude, double longitude);

    /**
     * Returns true if the origin and destination depots are in different cities,
     * meaning the delivery requires an intercity depot-to-depot transfer leg.
     *
     * @param originDepot      depot closest to the pickup address
     * @param destinationDepot depot closest to the dropoff address
     * @return true if an intercity transfer is required
     */
    boolean requiresIntercityTransfer(Depot originDepot, Depot destinationDepot);
}
