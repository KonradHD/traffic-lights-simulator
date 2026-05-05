package com.traffic_lights.dto;

import static com.traffic_lights.dto.VehiclesDTO.createVehiclesDTO;

import java.util.ArrayList;
import java.util.List;

public record SimulationOutput(
    List<VehiclesDTO> stepStatuses
) {

    public void addVehicles(List<String> vehicles){
        VehiclesDTO dto = createVehiclesDTO(vehicles);
        stepStatuses.add(dto);
    }

    public static SimulationOutput createEmptySimOutput(){
        return new SimulationOutput(new ArrayList<>());
    }
}
