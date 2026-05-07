package com.traffic_lights.intersection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class IntersectionStats {
    private int phaseChanges;
    private int phaseDuration;
    private int stepsNumber;
    private int vehiclesLeftNumber;
    private int vehiclesWaitingNumber;

    public void increasePhaseChanges() {
        this.phaseChanges++;
    }

    public void increaseStepsWithoutChange() {
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

    public void increaseVehiclesWaitingNumber() {
        this.vehiclesWaitingNumber++;
    }

    public void removeWaitingVehicles(int count) {
        this.vehiclesWaitingNumber -= count;
    }

    @Override
    public String toString() {
        return """
               \n=== Simulation state ===
               Steps number: %d
               Intersection phase changes: %d
               Steps in current phase: %d
               Vehicles left count: %d
               Vehicles waiting count:  %d
               ======================
               """.formatted(stepsNumber, phaseChanges, phaseDuration, vehiclesLeftNumber, vehiclesWaitingNumber);
    }
}
