package com.traffic_lights.commands;

import java.util.Collections;
import java.util.List;

import com.traffic_lights.components.Direction;
import com.traffic_lights.components.Intersection;
import com.traffic_lights.dto.Vehicle;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record AddVehicleCommand(

    String vehicleId, 
    Direction startRoad, 
    Direction endRoad
    
) implements Command {

    @Override
    public List<String> execute(Intersection intersection) {
        log.info("Adding vehicle - {} to queue", vehicleId);
        Vehicle vehicle = new Vehicle(vehicleId, startRoad, endRoad);
        intersection.addVehicleToQueue(vehicle);
        return Collections.emptyList();
    }
}
