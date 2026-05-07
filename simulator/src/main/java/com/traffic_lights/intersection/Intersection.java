package com.traffic_lights.intersection;

import com.traffic_lights.config.IntersectionConfig;
import com.traffic_lights.dto.Vehicle;
import com.traffic_lights.dto.intersection.IntersectionLayout;
import com.traffic_lights.model.Direction;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
public abstract class Intersection {

    @Getter
    protected String intersectionType;

    @Getter
    protected IntersectionStats stats;

    protected List<IntersectionPhase> phases;
    protected int currentPhaseIndex;


    public Intersection(String type) {
        initIntersectionConfig(type);

        if(!phases.isEmpty()){
            int randomMax = phases.size();
            int randomIndex = ThreadLocalRandom.current().nextInt(randomMax);
            this.currentPhaseIndex = randomIndex;
            log.info("Selected random starting phase index: {}", randomIndex);
        }

        stats = new IntersectionStats();
    }

    private void initIntersectionConfig(String type){
        IntersectionConfig.loadConfig();
        IntersectionLayout layoutTemplate = IntersectionConfig.getLayoutForType(type.toUpperCase());

        phases = layoutTemplate
                .phases()
                .stream()
                .map(dto -> new IntersectionPhase(dto.paths(), dto.basicDuration(), dto.basicDuration(), 0))
                .toList();
    }


    protected abstract void activateCurrentPhase();


    protected void switchToNextPhase() {
        currentPhaseIndex++;
        if (currentPhaseIndex >= phases.size()) {
            currentPhaseIndex = 0;
        }
        resetWaitingTime();
        IntersectionPhase currentPhase = phases.get(currentPhaseIndex);
        setOptimalPhaseTime(currentPhase);

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
        resetWaitingTime();
        IntersectionPhase currentPhase = phases.get(currentPhaseIndex);
        setOptimalPhaseTime(currentPhase);

        this.stats.increasePhaseChanges();
        this.stats.resetPhaseDuration();
        activateCurrentPhase();
        log.info("Switched to the intersection phase: {}", this.currentPhaseIndex);
    }

    protected abstract void setOptimalPhaseTime(IntersectionPhase phase);

    protected int getCycleBasicDuration() {
        return phases.stream()
                .mapToInt(IntersectionPhase::getBasicDuration)
                .sum();
    }

    protected void resetWaitingTime(){
        for(IntersectionPhase phase : phases){
            phase.setWaitingTime(0);
        }
    }

    public abstract void addVehicleToQueue(Vehicle vehicle);

    protected abstract boolean isPrioritized(Direction endDirection, IntersectionPhase phase, boolean rightArrow);

    protected abstract List<Vehicle> findVehiclesForCurrentPhase();

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

    protected int determineNextPhaseIndex() {
        int bestPhaseIndex = (currentPhaseIndex + 1) % phases.size();
        int maxVehicles = 0;

        for (int i = 0; i < phases.size(); i++) {
            if (i == currentPhaseIndex) continue;
            int potentialVehicles = countPotentialVehiclesForPhase(phases.get(i));

            if (potentialVehicles > maxVehicles) {
                maxVehicles = potentialVehicles;
                bestPhaseIndex = i;
            }
        }

        return bestPhaseIndex;
    }


    public List<String> processStep() {
        IntersectionPhase currentPhase = phases.get(currentPhaseIndex);
        this.stats.increasePhaseDuration();

        if (this.stats.getPhaseDuration() >= currentPhase.getOptimalDuration()) {
            int nextPhaseIndex = determineNextPhaseIndex();
            switchToPhase(nextPhaseIndex);
            currentPhase = phases.get(currentPhaseIndex);
            setOptimalPhaseTime(currentPhase);
        }

        List<Vehicle> leftVehicles = findVehiclesForCurrentPhase();

        if (leftVehicles.isEmpty() && this.stats.getVehiclesWaitingNumber() > 0) {
            boolean isOptimized = optimizeCurrentPhase();
            if (isOptimized) {
                currentPhase = phases.get(currentPhaseIndex);
                setOptimalPhaseTime(currentPhase);
                leftVehicles = findVehiclesForCurrentPhase();
            }
        }

        this.stats.increaseStepsNumber();
        this.stats.addVehiclesLeft(leftVehicles.size());

        Map<Direction, Long> vehiclesByDirection = leftVehicles.stream()
                .collect(Collectors.groupingBy(Vehicle::startRoad, Collectors.counting()));

        for (Map.Entry<Direction, Long> entry : vehiclesByDirection.entrySet()) {
            Direction direction = entry.getKey();
            int amount = entry.getValue().intValue();

            this.stats.removeWaitingVehicles(direction, amount);
        }

        return leftVehicles.stream().map(Vehicle::id).toList();
    }

}
