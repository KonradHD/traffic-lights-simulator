package com.traffic_lights.intersection;

import com.traffic_lights.model.Direction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public class IntersectionStats {
    private int phaseChanges;
    private int phaseDuration;
    private int stepsNumber;
    private int vehiclesLeftNumber;
    private final Map<Direction, Integer> vehiclesWaiting = new HashMap<>();

    public IntersectionStats(){
        this.phaseChanges = 0;
        this.phaseDuration = 0;
        this.stepsNumber = 0;
        this.vehiclesLeftNumber = 0;
        for(Direction direction : Direction.values()){
            vehiclesWaiting.put(direction, 0);
        }
    }


    public void increasePhaseChanges() {
        this.phaseChanges++;
    }

    public void increasePhaseDuration() {
        this.phaseDuration++;
    }

    public void increaseStepsNumber() {
        this.stepsNumber++;
    }

    public void addVehiclesLeft(int count) {
        this.vehiclesLeftNumber += count;
    }

    public void resetPhaseDuration(){
        this.phaseDuration = 0;
    }

    public void increaseVehiclesWaiting(Direction direction) {
        this.vehiclesWaiting.compute(direction, (k, waiting) -> waiting + 1);
    }

    public void addWaitingVehicles(Direction direction, int count){
        this.vehiclesWaiting.compute(direction, (k, waiting) -> waiting + count);
    }

    public void removeWaitingVehicles(Direction direction, int count) {
        this.vehiclesWaiting.compute(direction, (k, waiting) -> waiting - count);
    }

    public Integer getVehiclesWaitingNumber() {
        int sum = 0;
        for(Map.Entry<Direction, Integer> entry : vehiclesWaiting.entrySet()){
            sum += entry.getValue();
        }
        return sum;
    }

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
