package com.traffic_lights.intersection;

import com.traffic_lights.model.Direction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.HashMap;
import java.util.Map;

/**
 * Data container for tracking and reporting real-time statistics of an intersection.
 * <p>
 * This class serves as the telemetry system for the simulation, maintaining counters
 * for phase transitions, time steps, and vehicle throughput. It also provides a
 * breakdown of congestion levels by tracking waiting vehicles for each cardinal direction.
 * </p>
 */
@Getter
@AllArgsConstructor
public class IntersectionStats {
    private int phaseChanges;
    private int phaseDuration;
    private int stepsNumber;
    private int vehiclesLeftNumber;
    private final Map<Direction, Integer> vehiclesWaiting = new HashMap<>();

    /**
     * Initializes a new statistics object with all counters set to zero.
     */
    public IntersectionStats(){
        this.phaseChanges = 0;
        this.phaseDuration = 0;
        this.stepsNumber = 0;
        this.vehiclesLeftNumber = 0;
        for(Direction direction : Direction.values()){
            vehiclesWaiting.put(direction, 0);
        }
    }

    /**
     * Increments the phase change counter.
     */
    public void increasePhaseChanges() {
        this.phaseChanges++;
    }

    /**
     * Increments the duration counter for the current active phase.
     */
    public void increasePhaseDuration() {
        this.phaseDuration++;
    }

    /**
     * Increments the total simulation step counter.
     */
    public void increaseStepsNumber() {
        this.stepsNumber++;
    }

    /**
     * Adds a specific amount of vehicles to the total throughput count.
     *
     * @param count The number of vehicles that left the intersection in the current step.
     */
    public void addVehiclesLeft(int count) {
        this.vehiclesLeftNumber += count;
    }

    /**
     * Resets the current phase duration timer to zero.
     */
    public void resetPhaseDuration(){
        this.phaseDuration = 0;
    }

    /**
     * Increments the waiting vehicle count for a specific direction by one.
     *
     * @param direction The approach direction where a vehicle was added.
     */
    public void increaseVehiclesWaiting(Direction direction) {
        this.vehiclesWaiting.compute(direction, (k, waiting) -> waiting + 1);
    }

    /**
     * Adds a specified number of vehicles to the waiting count for a direction.
     *
     * @param direction The approach direction.
     * @param count The number of vehicles to add.
     */
    public void addWaitingVehicles(Direction direction, int count){
        this.vehiclesWaiting.compute(direction, (k, waiting) -> waiting + count);
    }

    /**
     * Decrements the waiting vehicle count for a specific direction.
     *
     * @param direction The approach direction where vehicles successfully departed.
     * @param count     The number of vehicles that left the queue.
     */
    public void removeWaitingVehicles(Direction direction, int count) {
        this.vehiclesWaiting.compute(direction, (k, waiting) -> waiting - count);
    }

    /**
     * Calculates the total number of vehicles currently waiting across all approaches.
     *
     * @return The sum of all values in the {@code vehiclesWaiting} map.
     */
    public Integer getVehiclesWaitingNumber() {
        int sum = 0;
        for(Map.Entry<Direction, Integer> entry : vehiclesWaiting.entrySet()){
            sum += entry.getValue();
        }
        return sum;
    }

    /**
     * Generates a formatted summary of the current simulation state.
     * <p>
     * Includes total steps, phase changes, time spent in current phase,
     * total throughput, and a cardinal breakdown of the queues.
     * </p>
     *
     * @return A multi-line string representing the simulation status.
     */
    @Override
    public String toString() {
        return """
               \n=== Simulation state ===
               Steps number: %d
               Intersection phase changes: %d
               Steps in current phase: %d
               Vehicles left count: %d
               Vehicles waiting count:
               south: %d, north: %s, west: %d, east: %d
               ======================
               """.formatted(stepsNumber, phaseChanges, phaseDuration, vehiclesLeftNumber,
                            vehiclesWaiting.get(Direction.SOUTH), vehiclesWaiting.get(Direction.NORTH),
                            vehiclesWaiting.get(Direction.WEST), vehiclesWaiting.get(Direction.EAST));
    }
}
