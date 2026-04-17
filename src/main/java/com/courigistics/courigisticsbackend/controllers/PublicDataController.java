package com.courigistics.courigisticsbackend.controllers;

import com.courigistics.courigisticsbackend.dto.responses.courier.VehicleDetailDTO;
import com.courigistics.courigisticsbackend.dto.responses.publicdata.MapDataResponse;
import com.courigistics.courigisticsbackend.entities.Courier;
import com.courigistics.courigisticsbackend.entities.Depot;
import com.courigistics.courigisticsbackend.entities.Vehicles;
import com.courigistics.courigisticsbackend.entities.enums.CourierStatus;
import com.courigistics.courigisticsbackend.entities.enums.DepotStatus;
import com.courigistics.courigisticsbackend.repositories.CourierRepository;
import com.courigistics.courigisticsbackend.repositories.DepotRepository;
import com.courigistics.courigisticsbackend.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Slf4j
public class PublicDataController {

    private final DepotRepository depotRepository;
    private final CourierRepository courierRepository;
    private final VehicleRepository vehicleRepository;

    @GetMapping("/map-data")
    public ResponseEntity<Map<String, Object>> getMapData() {
        log.info("Fetching map data for public view");

        List<Depot> activeDepots = depotRepository.findByStatus(DepotStatus.ACTIVE);
        List<Courier> availableCouriers = courierRepository.findByStatus(CourierStatus.ACTIVE).stream()
                .filter(Courier::getAvailableForAssignment)
                .filter(c -> c.getCurrentLat() != null && c.getCurrentLon() != null)
                .toList();

        List<MapDataResponse.DepotItem> depotItems = activeDepots.stream()
                .map(d -> MapDataResponse.DepotItem.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .city(d.getCity())
                        .code(d.getCode())
                        .latitude(d.getLatitude())
                        .longitude(d.getLongitude())
                        .radiusKm(d.getCoverageRadiusKm())
                        .build())
                .collect(Collectors.toList());

        List<MapDataResponse.CourierItem> courierItems = availableCouriers.stream()
                .map(c -> {
                    Vehicles v = vehicleRepository.findByCourier_id(c.getId()).orElse(null);
                    VehicleDetailDTO vehicleDetail = v != null ? VehicleDetailDTO.builder()
                            .type(v.getVehicleType())
                            .make(v.getMake())
                            .model(v.getModel())
                            .color(v.getVehicleColor())
                            .licensePlate(v.getLicencePlate())
                            .build() : null;

                    return MapDataResponse.CourierItem.builder()
                            .id(c.getId())
                            .firstName(c.getFirstName())
                            .latitude(c.getCurrentLat())
                            .longitude(c.getCurrentLon())
                            .vehicle(vehicleDetail)
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", MapDataResponse.builder()
                        .depots(depotItems)
                        .couriers(courierItems)
                        .build()
        ));
    }
}
