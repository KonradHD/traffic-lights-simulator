package com.traffic_lights.components.intersection;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import com.traffic_lights.components.*;
import com.traffic_lights.components.lights.RoadLights;
import com.traffic_lights.dto.Vehicle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Intersection {

    @Getter
    protected String intersectionType;

    @Getter
    protected IntersectionStats stats;

    protected Map<Direction, RoadLights> roadsLights;
    protected List<IntersectionPhase> phases;
    protected int currentPhaseIndex;


    protected void activateCurrentPhase() {
        if(phases.isEmpty()){
            log.warn("Cannot initialize lights: phases list is empty!");
            return;
        }

        IntersectionPhase currentPhase = phases.get(currentPhaseIndex);

        for (Map.Entry<Direction, RoadLights> entry : roadsLights.entrySet()) {
            Direction direction = entry.getKey();
            RoadLights hardwareLights = entry.getValue();

            List<Turn> allowedTurns = currentPhase.getTurns(direction);
            hardwareLights.applyAllowedTurns(allowedTurns);
        }
    }

    protected void switchToNextPhase() {
        currentPhaseIndex++;
        if (currentPhaseIndex >= phases.size()) {
            currentPhaseIndex = 0;
        }

        this.stats.resetPhaseDuration();
        this.stats.increasePhaseChanges();
        activateCurrentPhase();
        log.info("Switched to the next intersection phase: {}", this.currentPhaseIndex);
    }

    protected void switchToPhase(int index) {
        if (index >= phases.size()) {
            log.warn("Trying to switch to the non existence phase");
            return;
        }

        this.currentPhaseIndex = index;

        this.stats.increasePhaseChanges();
        this.stats.resetPhaseDuration();
        activateCurrentPhase();
        log.info("Switched to the intersection phase: {}", this.currentPhaseIndex);
    }

    public abstract void addVehicleToQueue(Vehicle vehicle);

    protected abstract boolean isPrioritized(Direction endDirection, IntersectionPhase phase, boolean rightArrow);

    protected abstract List<String> findVehiclesForCurrentPhase();

    protected abstract int countPotentialVehiclesForPhase(IntersectionPhase phase);

    protected boolean optimizeCurrentPhase() {
        log.info("Optimizing vehicles flow...");

        int maxVehiclesToLeave = 0;
        IntersectionPhase bestPhase = null;
        int newPhaseIndex = currentPhaseIndex + 1;

        while(newPhaseIndex % phases.size() != currentPhaseIndex){

            IntersectionPhase evaluatedPhase = phases.get(newPhaseIndex % phases.size());
            int potentialVehicles = countPotentialVehiclesForPhase(evaluatedPhase);

            if (potentialVehicles > maxVehiclesToLeave) {
                maxVehiclesToLeave = potentialVehicles;
                bestPhase = evaluatedPhase;
            }
            newPhaseIndex++;
        }

        if (phases.indexOf(bestPhase) != currentPhaseIndex && maxVehiclesToLeave > 0) {
            switchToPhase(phases.indexOf(bestPhase));
            return true;
        }
        return false;
    }


    public List<String> processStep() {
        IntersectionPhase currentPhase = phases.get(currentPhaseIndex);
        if(this.stats.getPhaseDuration() == currentPhase.getMaxDuration()){
            switchToNextPhase();
        }

        List<String> leftVehicles = findVehiclesForCurrentPhase();

        if(leftVehicles.isEmpty() && this.stats.getVehiclesWaitingNumber() > 0){
            boolean isOptimized = optimizeCurrentPhase();
            if(isOptimized){
                leftVehicles = findVehiclesForCurrentPhase();
            }
        }else{
            this.stats.increaseStepsWithoutChange();
        }

        this.stats.increaseStepsNumber();
        this.stats.addVehiclesLeft(leftVehicles.size());
        this.stats.removeWaitingVehicles(leftVehicles.size());
        return leftVehicles;
    }

}
