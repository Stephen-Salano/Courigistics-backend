package com.courigistics.courigisticsbackend.dto.responses.courier;

import com.courigistics.courigisticsbackend.entities.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDetailDTO {
    private VehicleType type;
    private String make;
    private String model;
    private String color;
    private String licensePlate;
}
