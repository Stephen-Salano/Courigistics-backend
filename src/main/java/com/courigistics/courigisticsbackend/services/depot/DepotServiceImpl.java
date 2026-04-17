package com.courigistics.courigisticsbackend.services.depot;

import com.courigistics.courigisticsbackend.entities.Depot;
import com.courigistics.courigisticsbackend.exceptions.BadRequestException;
import com.courigistics.courigisticsbackend.services.geo.GeoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepotServiceImpl implements DepotService {

    private final GeoService geoService;

    @Override
    public Depot findNearestDepotFor(double latitude, double longitude) {
        return findOptionalNearestDepotFor(latitude, longitude)
                .orElseThrow(() -> new BadRequestException("No Depot covers this location. Delivery is not available in this area"));
    }

    @Override
    public Optional<Depot> findOptionalNearestDepotFor(double latitude, double longitude) {
        log.debug("Finding optional nearest depot for ({}, {})", latitude, longitude);
        return geoService.findNearestDepot(latitude, longitude);
    }

    @Override
    public boolean isWithinCoverage(double latitude, double longitude) {
        return geoService.isWithinCoverage(latitude, longitude);
    }

    @Override
    public boolean requiresIntercityTransfer(Depot originDepot, Depot destinationDepot) {
        if (originDepot == null || destinationDepot == null){
            throw new BadRequestException("Both origin and destination depots must be present");
        }

        boolean intercity = !originDepot.getCity().equalsIgnoreCase(destinationDepot.getCity());
        log.debug("Intercity transfer required between {} and {}: {}",
                originDepot.getCity(), destinationDepot.getCity(), intercity);
        return intercity;
    }
}
