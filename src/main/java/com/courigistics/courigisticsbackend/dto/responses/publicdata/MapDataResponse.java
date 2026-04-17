package com.courigistics.courigisticsbackend.dto.responses.publicdata;

import com.courigistics.courigisticsbackend.dto.responses.courier.VehicleDetailDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapDataResponse {
    private List<DepotItem> depots;
    private List<CourierItem> couriers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepotItem {
        private UUID id;
        private String name;
        private String city;
        private String code;
        private Double latitude;
        private Double longitude;
        private Double radiusKm;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourierItem {
        private UUID id;
        private String firstName;
        private Double latitude;
        private Double longitude;
        private VehicleDetailDTO vehicle;
    }
}
