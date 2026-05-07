package com.traffic_lights.intersection;

import com.traffic_lights.intersection.phase.IntersectionPhase;
import com.traffic_lights.intersection.phase.PhaseMetricsProvider;
import com.traffic_lights.intersection.phase.PhaseScheduler;
import com.traffic_lights.model.Vehicle;
import com.traffic_lights.dto.intersection.IntersectionParameters;
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
    protected IntersectionParameters parameters;


    public Intersection(String type, List<IntersectionPhase> phases, IntersectionParameters parameters) {
        this.intersectionType = type;
        this.phases = phases;
        this.parameters = parameters;
        this.stats = new IntersectionStats();

        if (!phases.isEmpty()) {
            this.currentPhaseIndex = ThreadLocalRandom.current().nextInt(phases.size());
            log.info("Selected random starting phase index: {}", this.currentPhaseIndex);
        }
    }


    protected void switchToPhase(int index) {
        if (index >= phases.size() || index == currentPhaseIndex) {
            log.warn("Invalid phase switch attempt to index: {}", index);
            return;
        }

        this.currentPhaseIndex = index;
        IntersectionPhase currentPhase = phases.get(currentPhaseIndex);

        resetWaitingTime();
        setOptimalPhaseTime(currentPhase);
        activateCurrentPhase();

        this.stats.increasePhaseChanges();
        this.stats.resetPhaseDuration();
        log.info("Switched to the intersection phase: {}", this.currentPhaseIndex);
    }

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


    protected int determineNextPhaseIndex() {
        int bestPhaseIndex = (currentPhaseIndex + 1) % phases.size();
        double maxPriority = -1.0;

        for (int i = 0; i < phases.size(); i++) {
            if (i == currentPhaseIndex) continue;
            IntersectionPhase phase = phases.get(currentPhaseIndex);
            double phasePriority = calculatePhasePriority(phase);

            if (phasePriority > maxPriority) {
                maxPriority = phasePriority;
                bestPhaseIndex = i;
            }
        }
        return bestPhaseIndex;
    }

    private void increaseWaitingTime() {
        for(IntersectionPhase phase : phases){
            if(phases.indexOf(phase) == currentPhaseIndex){
                continue;
            }

            if(isAnyVehicleWaiting(phase)){
                phase.increaseWaitingTime();
            }
        }
    }


    private void performPhaseSwitch() {
        int nextPhaseIndex = determineNextPhaseIndex();
        switchToPhase(nextPhaseIndex);
    }


    public List<String> processStep() {
        IntersectionPhase currentPhase = phases.get(currentPhaseIndex);
        if (this.stats.getPhaseDuration() >= currentPhase.getOptimalDuration()) {
            performPhaseSwitch();
        }

        List<Vehicle> leftVehicles = findVehiclesForCurrentPhase();

        if (leftVehicles.isEmpty() && this.stats.getVehiclesWaitingNumber() > 0) {
            performPhaseSwitch();
            leftVehicles = findVehiclesForCurrentPhase();
        }

        this.stats.increasePhaseDuration();
        this.stats.increaseStepsNumber();
        increaseWaitingTime();
        this.stats.addVehiclesLeft(leftVehicles.size());

        Map<Direction, Long> vehiclesByDirection = leftVehicles.stream()
                .collect(Collectors.groupingBy(Vehicle::startRoad, Collectors.counting()));

        for (Map.Entry<Direction, Long> entry : vehiclesByDirection.entrySet()) {
            this.stats.removeWaitingVehicles(entry.getKey(), entry.getValue().intValue());
        }

        return leftVehicles.stream().map(Vehicle::id).toList();
    }

    protected abstract void setOptimalPhaseTime(IntersectionPhase phase);
    public abstract void addVehicleToQueue(Vehicle vehicle);
    protected abstract boolean isPrioritized(Direction endDirection, IntersectionPhase phase, boolean rightArrow);
    protected abstract List<Vehicle> findVehiclesForCurrentPhase();
    protected abstract int countPotentialVehiclesForPhase(IntersectionPhase phase);
    protected abstract boolean isAnyVehicleWaiting(IntersectionPhase phase);
    protected abstract void activateCurrentPhase();
    protected abstract double calculatePhasePriority(IntersectionPhase phase);
}
