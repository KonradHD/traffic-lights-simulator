package com.traffic_lights.dto;

import java.util.List;

/**
 * Data Transfer Object representing the collection of vehicles that successfully
 * cleared the intersection during a single discrete simulation step.
 */
public record VehiclesDTO(
    List<String> leftVehicles
) {

    /**
     * Static factory method to instantiate a new {@code VehiclesDTO}.
     *
     * @param vehicles A list of vehicle IDs that cleared the intersection.
     * @return A new instance of {@link VehiclesDTO} containing the provided IDs.
     */
    public static VehiclesDTO createVehiclesDTO(List<String> vehicles){
        return new VehiclesDTO(vehicles);
    }
    
}
