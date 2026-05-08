package com.traffic_lights.dto;

import static com.traffic_lights.dto.VehiclesDTO.createVehiclesDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object representing the final results of a simulation run.
 * <p>
 * This record serves as the structured container for the simulation's history.
 * It aggregates the status of each discrete time step, specifically tracking
 * which vehicles successfully cleared the intersection during that increment.
 * </p>
 */
public record SimulationOutput(
    List<VehiclesDTO> stepStatuses
) {

    /**
     * Records the vehicles that left the intersection in a new simulation step.
     * <p>
     * This method wraps the provided list of vehicle IDs into a {@link VehiclesDTO}
     * and appends it to the historical record of step statuses.
     * </p>
     *
     * @param vehicles A list of unique string identifiers for vehicles that departed.
     */
    public void addVehicles(List<String> vehicles){
        VehiclesDTO dto = createVehiclesDTO(vehicles);
        stepStatuses.add(dto);
    }

    /**
     * Static factory method to initialize a new, empty simulation result container.
     *
     * @return A {@code SimulationOutput} instance with an initialized, empty
     * list of step statuses.
     */
    public static SimulationOutput createEmptySimOutput(){
        return new SimulationOutput(new ArrayList<>());
    }
}
