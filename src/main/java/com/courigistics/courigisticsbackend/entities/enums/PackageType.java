package com.courigistics.courigisticsbackend.entities.enums;

/**
 * Answers the question what kind of item is this? - the business classification
 * Tells us the nature of the shipment for pricing, handling and tracking purposes
 */
public enum PackageType {
    DOCUMENT, PARCEL, FRAGILE, EXTERNAL_ORDER
}
