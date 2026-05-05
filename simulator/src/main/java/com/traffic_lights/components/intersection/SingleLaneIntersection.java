package com.traffic_lights.components.intersection;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import com.traffic_lights.components.*;
import com.traffic_lights.dto.Vehicle;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SingleLaneIntersection extends Intersection {

    private final Map<Direction, Queue<Vehicle>> roads = new HashMap<>();


    public SingleLaneIntersection(String type) {

        roads.put(Direction.EAST, new ArrayDeque<>());
        roads.put(Direction.WEST, new ArrayDeque<>());
        roads.put(Direction.NORTH, new ArrayDeque<>());
        roads.put(Direction.SOUTH, new ArrayDeque<>());
        stats = new IntersectionStats(0, 0, 0, 0, 0);

        switch (type.toUpperCase()) {
            case "STANDARD" -> {
                intersectionType = type.toUpperCase();
                phases = PhasesBuilder.createStandardPhases();
                roadsLights = IntersectionType.createStandard().getRoadsConfig();
            }
            case "LEFT_TURN_ARROWS" -> {
                intersectionType = type.toUpperCase();
                phases = PhasesBuilder.createLeftArrowsPhases();
                roadsLights = IntersectionType.createWithLeftTurnArrows().getRoadsConfig();
            }
            case "RIGHT_TURN_ARROWS" -> {
                intersectionType = type.toUpperCase();
                phases = PhasesBuilder.createRightTurnArrowsPhases();
                roadsLights = IntersectionType.createWithRightTurnArrows().getRoadsConfig();
            }
            case "SPLIT_PHASES" -> {
                intersectionType = type.toUpperCase();
                phases = PhasesBuilder.createSplitPhases();
                roadsLights = IntersectionType.createSplitPhases().getRoadsConfig();
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


    public void addVehicleToQueue(Vehicle vehicle) {
        Queue<Vehicle> queue = roads.get(vehicle.startRoad());
        if (queue == null) {
            throw new IllegalArgumentException("Unknown direction: " + vehicle.startRoad());
        }
        queue.add(vehicle);
        this.stats.increaseVehiclesWaitingNumber();
    }

    protected boolean isPrioritized(Direction endDirection, IntersectionPhase phase, boolean rightArrow) {
        Direction startingDirection;
        if(rightArrow){
            startingDirection = endDirection.getLeftDirection();
        }else{
            startingDirection = endDirection.getOpposite();
        }

        Queue<Vehicle> queue = roads.get(startingDirection);
        if (queue == null || queue.isEmpty()) {
            return true;
        }

        Vehicle vehicle = queue.peek();
        Turn intendedTurn = startingDirection.calculateTurn(vehicle.endRoad());
        List<Turn> availableTurns = phase.getTurns(startingDirection);

        if(rightArrow){
            return !(availableTurns != null && availableTurns.contains(intendedTurn) && intendedTurn == Turn.STRAIGHT);
        }else{
            return !(availableTurns != null && availableTurns.contains(intendedTurn) && (intendedTurn == Turn.STRAIGHT || intendedTurn == Turn.RIGHT));
        }
    }


    protected List<String> findVehiclesForCurrentPhase(){
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

                if(intendedTurn == Turn.LEFT && !isPrioritized(direction, currentPhase, false)){
                    log.info("{} is turning left and is not prioritized", vehicle.id());
                    continue;
                }

                if(intendedTurn == Turn.RIGHT && !isPrioritized(direction, currentPhase, true)){
                    log.info("{} is turning right with conditional arrow and is not prioritized", vehicle.id());
                    continue;
                }

                queue.poll();
                leftVehicles.add(vehicle.id());
            }
        }
        return leftVehicles;
    }

    protected int countPotentialVehiclesForPhase(IntersectionPhase phase) {
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

}

