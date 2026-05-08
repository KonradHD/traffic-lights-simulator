package com.traffic_lights.commands;

import java.util.Collections;
import java.util.List;

import com.traffic_lights.model.Direction;
import com.traffic_lights.intersection.Intersection;
import com.traffic_lights.model.Vehicle;
import lombok.extern.slf4j.Slf4j;


/**
 * A command responsible for introducing a new vehicle into the traffic simulation.
 * <p>
 * This command encapsulates the necessary data to spawn a vehicle and enqueue it
 * at the correct starting road of the intersection.
 * </p>
 *
 * @param vehicleId The unique identifier for the vehicle being added.
 * @param startRoad The geographical direction from which the vehicle enters the intersection.
 * @param endRoad The geographical direction indicating the vehicle's destination.
 */
@Slf4j
public record AddVehicleCommand(

    String vehicleId, 
    Direction startRoad, 
    Direction endRoad
    
) implements Command {

    /**
     * Executes the command by instantiating a new {@link Vehicle} and adding it
     * to the appropriate approach queue within the provided intersection.
     *
     * @param intersection The intersection state manager where the vehicle will be enqueued.
     * @return An empty list ({@link Collections#emptyList()}), because adding a vehicle
     * to the queue does not trigger any vehicles to leave the intersection.
     * @throws NullPointerException if the provided intersection is null.
     */
    @Override
    public List<String> execute(Intersection intersection) {
        log.info("Adding vehicle - {} to queue", vehicleId);
        Vehicle vehicle = new Vehicle(vehicleId, startRoad, endRoad);
        intersection.addVehicleToQueue(vehicle);
        return Collections.emptyList();
    }
}
