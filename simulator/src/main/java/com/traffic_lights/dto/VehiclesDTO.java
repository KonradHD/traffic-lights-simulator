package com.traffic_lights.dto;

import java.util.List;

public record VehiclesDTO(
    List<String> leftVehicles
) {
    public static VehiclesDTO createVehiclesDTO(List<String> vehicles){
        return new VehiclesDTO(vehicles);
    }
    
}
