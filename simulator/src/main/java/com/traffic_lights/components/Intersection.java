package com.traffic_lights.components;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import com.traffic_lights.dto.Vehicle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Intersection {

    @Getter
    private final String intersectionType;

    @Getter
    private final IntersectionStats stats;

    private final Map<Direction, Queue<Vehicle>> roads = new HashMap<>();
    private final Map<Direction, RoadLights> roadsLights;
    private List<IntersectionPhase> phases;
    private int currentPhaseIndex;

    public Intersection(String type) {

        roads.put(Direction.EAST, new ArrayDeque<>());
        roads.put(Direction.WEST, new ArrayDeque<>());
        roads.put(Direction.NORTH, new ArrayDeque<>());
        roads.put(Direction.SOUTH, new ArrayDeque<>());
        this.stats = new IntersectionStats(0, 0, 0, 0, 0);

        switch (type.toUpperCase()) {
            case "STANDARD" -> {
                intersectionType = type.toUpperCase();
                phases = PhasesBuilder.createStandardPhases();
                roadsLights = IntersectionType.createStandard().getRoadsConfig();
            }
            case "LEFT_TURN_ARROWS" -> {
                intersectionType = type.toUpperCase();
                roadsLights = IntersectionType.createWithLeftTurnArrows().getRoadsConfig();
            }
            case "RIGHT_TURN_ARROWS" -> {
                intersectionType = type.toUpperCase();
                roadsLights = IntersectionType.createWithRightTurnArrows().getRoadsConfig();
            }
            default ->
                throw new IllegalArgumentException("Unknown intersection type: " + type);
        }
        int randomMax = phases.size();
        int randomIndex = ThreadLocalRandom.current().nextInt(randomMax);
        this.currentPhaseIndex = randomIndex;
        log.info("Selected random starting phase index: {}", randomIndex);

        activateCurrentPhase();

        log.info("Created {} intersection", this.intersectionType);
    }


    public void activateCurrentPhase() {
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

    public void switchToNextPhase() {
        currentPhaseIndex++;
        if (currentPhaseIndex >= phases.size()) {
            currentPhaseIndex = 0;
        }

        this.stats.resetPhaseDuration();
        this.stats.increasePhaseChanges();
        activateCurrentPhase();
        log.info("Switched to the next intersection phase: {}", this.currentPhaseIndex);
    }

    public void switchToPhase(int index) {
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


    public void addVehicleToQueue(Vehicle vehicle) {
        Queue<Vehicle> queue = roads.get(vehicle.startRoad());
        if (queue == null) {
            throw new IllegalArgumentException("Unknown direction: " + vehicle.startRoad());
        } 
        queue.add(vehicle);
        this.stats.increaseVehiclesWaitingNumber();
    }

    // TODO: dla wielu pasów
    private List<String> findVehiclesForPhase(){
        IntersectionPhase currentPhase = phases.get(currentPhaseIndex);
        log.info("Looking for new vehicles in phase {}", currentPhaseIndex);

        List<String> leftVehicles = new ArrayList<>();
        List<Direction> dirs = currentPhase.getDirections();
        for(Direction direction : dirs){
            Queue<Vehicle> queue = roads.get(direction);

            if (queue == null || queue.isEmpty()) {
                continue;
            }
            List<Turn> availableTurns = currentPhase.getTurns(direction);
            Vehicle vehicle = queue.peek();
            Turn intendedTurn = direction.calculateTurn(vehicle.endRoad());
            if(intendedTurn != null && availableTurns.contains(intendedTurn)){
                queue.poll();
                leftVehicles.add(vehicle.id());
            }
        }
        return leftVehicles;
    }

    private int countPotentialVehiclesForPhase(IntersectionPhase phase) {
        int potentialCount = 0;

        for (Direction direction : phase.getDirections()) {
            Queue<Vehicle> queue = roads.get(direction);
            if (queue == null || queue.isEmpty()) continue;

            Vehicle firstCar = queue.peek();
            Turn intendedTurn = direction.calculateTurn(firstCar.endRoad());
            List<Turn> allowedTurns = phase.getTurns(direction);

            if (allowedTurns != null && allowedTurns.contains(intendedTurn)) {
                potentialCount++;
            }
        }
        return potentialCount;
    }

    private boolean optimizeCurrentPhase() {
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

   /* private List<String> optimizeCurrentPhase(){
        log.info("Optimizing vehicles flow");
        List<String> leftVehicles = new ArrayList<>();
        IntersectionPhase bestPhase = null;

        int newPhaseIndex = currentPhaseIndex + 1;
        while(newPhaseIndex % phases.size() != currentPhaseIndex){
            IntersectionPhase newPhase = phases.get(newPhaseIndex % phases.size());
            List<String> newVehicles = findVehiclesForPhase(newPhase);
            if(newVehicles.size() > leftVehicles.size()){
                leftVehicles = newVehicles;
                bestPhase = newPhase;
            }
            newPhaseIndex++;
        }

        if(bestPhase != null && !leftVehicles.isEmpty()){
            switchToPhase(phases.indexOf(bestPhase));
        }
        return leftVehicles;
    }*/


    public List<String> processStep() {
        IntersectionPhase currentPhase = phases.get(currentPhaseIndex);
        if(this.stats.getPhaseDuration() == currentPhase.getMaxDuration()){
            switchToNextPhase();
        }

        List<String> leftVehicles = findVehiclesForPhase();

        if(leftVehicles.isEmpty() && this.stats.getVehiclesWaitingNumber() > 0){
            boolean isOptimized = optimizeCurrentPhase();
            if(isOptimized){
                leftVehicles = findVehiclesForPhase();
            }
        }else{
            this.stats.increaseStepsWithoutChange();
        }

        this.stats.increaseStepsNumber();
        this.stats.addVehiclesLeft(leftVehicles.size());
        this.stats.removeWaitingVehicles(leftVehicles.size());
        return leftVehicles;
    }


     private boolean isRoadEmpty(Direction roadDirection) {
        return roads.get(roadDirection).isEmpty();
    }
}
