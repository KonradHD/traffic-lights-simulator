package com.traffic_lights.components.intersection;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import com.traffic_lights.components.*;
import com.traffic_lights.components.lights.RoadLights;
import com.traffic_lights.components.lights.TrafficLight;
import com.traffic_lights.config.IntersectionConfig;
import com.traffic_lights.dto.Vehicle;
import com.traffic_lights.dto.intersection.IntersectionLayout;
import com.traffic_lights.dto.intersection.TrafficLightDTO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.traffic_lights.components.lights.TrafficLight.createTrafficLightFromDTO;

@Slf4j
public abstract class Intersection {

    @Getter
    protected String intersectionType;

    @Getter
    protected IntersectionStats stats;

    protected final Map<Direction, RoadLights> roadsLights = new HashMap<>();
    protected List<IntersectionPhase> phases;
    protected int currentPhaseIndex;

    public Intersection(String type) {
        initIntersectionConfig(type);

        int randomMax = phases.size();
        int randomIndex = ThreadLocalRandom.current().nextInt(randomMax);
        this.currentPhaseIndex = randomIndex;
        log.info("Selected random starting phase index: {}", randomIndex);

        stats = new IntersectionStats(0, 0, 0, 0, 0);
        activateCurrentPhase();
    }

    private void initIntersectionConfig(String type){
        IntersectionConfig.loadConfig();
        IntersectionLayout layoutTemplate = IntersectionConfig.getLayoutForType(type.toUpperCase());

        for (Map.Entry<Direction, List<TrafficLightDTO>> entry : layoutTemplate.roadLights().entrySet()) {
            Direction direction = entry.getKey();
            List<TrafficLightDTO> jsonLights = entry.getValue();

            List<TrafficLight> physicalLights = new ArrayList<>();
            for (TrafficLightDTO light : jsonLights) {
                physicalLights.add(createTrafficLightFromDTO(light));
            }

            roadsLights.put(direction, new RoadLights(physicalLights));
        }

        phases = layoutTemplate
                .phases()
                .stream()
                .map(dto -> new IntersectionPhase(dto.paths(), dto.maxDuration()))
                .toList();
    }


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
