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

    public List<String> getLeftVehiclesIds(){
        List<String> allVehiclesIds = new ArrayList<>();
        stepStatuses.forEach(step -> allVehiclesIds.addAll(step.leftVehicles()));
        return allVehiclesIds;
    }

    public static SimulationOutput createEmptySimOutput(){
        return new SimulationOutput(new ArrayList<>());
    }
}
