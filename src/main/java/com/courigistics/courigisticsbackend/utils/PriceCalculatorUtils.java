package com.courigistics.courigisticsbackend.utils;

import com.courigistics.courigisticsbackend.entities.enums.PackageCategory;
import com.courigistics.courigisticsbackend.entities.enums.VehicleType;


import java.math.BigDecimal;
import java.math.RoundingMode;


public class PriceCalculatorUtils {

    private PriceCalculatorUtils(){
    }


    private static final BigDecimal PER_KM_RATE = BigDecimal.valueOf(15);
    private static final BigDecimal FRAGILE_SURCHARGE = BigDecimal.valueOf(200);
    private static final BigDecimal LARGE_ITEM_SURCHARGE = BigDecimal.valueOf(500);

    private static final BigDecimal BASE_BIKE  = BigDecimal.valueOf(150);
    private static final BigDecimal BASE_CAR   = BigDecimal.valueOf(300);
    private static final BigDecimal BASE_VAN   = BigDecimal.valueOf(800);
    private static final BigDecimal BASE_TRUCK = BigDecimal.valueOf(1500);

    /**
     * Calculates estimated delivery price in KES.
     *
     * Formula: base + (distanceKm × 15) + fragile(+200) + LARGE/XLARGE(+500)
     *
     * @param vehicleType     the tier selected by the customer
     * @param distanceKm      estimated route distance
     * @param isFragile       whether the package needs fragile handling
     * @param packageCategory size class of the package
     * @return estimated price in KES, rounded to 2 decimal places
     */
    public static BigDecimal calculatePrice(
            VehicleType vehicleType,
            double distanceKm,
            boolean isFragile,
            PackageCategory packageCategory
    ) {
        BigDecimal base = getBaseRate(vehicleType);
        BigDecimal distanceCost = PER_KM_RATE.multiply(BigDecimal.valueOf(distanceKm));

        BigDecimal fragileCost = isFragile ? FRAGILE_SURCHARGE : BigDecimal.ZERO;

        BigDecimal largeCost = (packageCategory == PackageCategory.LARGE
                || packageCategory == PackageCategory.X_LARGE)
                ? LARGE_ITEM_SURCHARGE : BigDecimal.ZERO;

        return base
                .add(distanceCost)
                .add(fragileCost)
                .add(largeCost)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal getBaseRate(VehicleType vehicleType) {
        return switch (vehicleType) {
            case BIKE  -> BASE_BIKE;
            case CAR   -> BASE_CAR;
            case VAN   -> BASE_VAN;
            case TRUCK -> BASE_TRUCK;
        };
    }
}
