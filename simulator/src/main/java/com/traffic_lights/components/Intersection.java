package com.traffic_lights.components;

import java.util.*;

import com.traffic_lights.config.StartingPhase;
import static com.traffic_lights.dto.VehiclesDTO.createVehiclesDTO;
import com.traffic_lights.dto.VehiclesDTO;

public class Intersection {
    
    private final Map<Direction, Queue<Vehicle>> roads = new HashMap<>();
    private final String intersectiontType;
    private final Map<Direction, RoadLights> roadsLights;
    private int lastPhaseChangeStep = 0;
    private int stepsNumber = 0;

    public Intersection(String type) {
        intersectiontType = type;
        switch (type.toUpperCase()) {
            case "STANDARD" -> {
                roadsLights = IntersectionType.createStandard().getRoadsConfig();
            }
            case "LEFT_TURN_ARROWS" -> {
                roadsLights = IntersectionType.createWithLeftTurnArrows().getRoadsConfig();
            }
            case "RIGHT_TURN_ARROWS" -> {
                roadsLights = IntersectionType.createWithRightTurnArrows().getRoadsConfig();
            }
            default ->
                throw new IllegalArgumentException("Unknown intersection type: " + type);
        }
        roads.put(Direction.EAST, new ArrayDeque<>());
        roads.put(Direction.WEST, new ArrayDeque<>());
        roads.put(Direction.NORTH, new ArrayDeque<>());
        roads.put(Direction.SOUTH, new ArrayDeque<>());
    }

    public String getIntersectionType(){
        return intersectiontType;
    }


    public void addVehicleToQueue(Vehicle vehicle) {
        Queue<Vehicle> queue = roads.get(vehicle.startRoad());
        if (queue == null) {
            throw new IllegalArgumentException("Unknown direction: " + vehicle.startRoad());
        } 
        queue.add(vehicle);
    }

    public void initRoadsLights() {
        StartingPhase.InitialSignal startSignal = StartingPhase.generateRandomStart();
        Direction startDir = startSignal.direction();
        Turn startTurn = startSignal.turn();

        RoadLights startingRoadsLights = roadsLights.get(startDir);
        RoadLights finishingRoadsLights = roadsLights.get(startDir.getDestinationDirection(startTurn));
        
        if (startingRoadsLights != null) {
            switch (startTurn) {
                case STRAIGHT -> {
                    if (startingRoadsLights.getMainLight() != null) {
                        startingRoadsLights.getMainLight().changeState(LightState.GREEN);
                        finishingRoadsLights.getMainLight().changeState(LightState.GREEN);
                    }
                }
                case LEFT -> {
                    if (startingRoadsLights.getLeftTurnArrow() != null) {
                        startingRoadsLights.getLeftTurnArrow().changeState(LightState.GREEN);
                        finishingRoadsLights.getRightTurnArrow().changeState(LightState.GREEN);
                    } else if (startingRoadsLights.getMainLight() != null) {
                        // jeśli skrzyżowanie nie ma lewoskrętu, zapalamy główne światło
                        startingRoadsLights.getMainLight().changeState(LightState.GREEN);
                        finishingRoadsLights.getMainLight().changeState(LightState.GREEN);
                    }
                }
                case RIGHT -> {
                    if (startingRoadsLights.getRightTurnArrow() != null) {
                        startingRoadsLights.getRightTurnArrow().changeState(LightState.GREEN);
                        finishingRoadsLights.getLeftTurnArrow().changeState(LightState.GREEN);
                    } else if (startingRoadsLights.getMainLight() != null) {
                        startingRoadsLights.getMainLight().changeState(LightState.GREEN);
                        finishingRoadsLights.getMainLight().changeState(LightState.GREEN);
                    }
                }
            }
        }
    }

    private void nextStepForAll(){
        for(Map.Entry<Direction, RoadLights> entry : roadsLights.entrySet()){
            entry.getValue().nextStepForAll();
        }
    }


    public List<String> processStep() {
        List<String> leftVehicles = new ArrayList<>();

        for(Map.Entry<Direction, RoadLights> entry : roadsLights.entrySet()){
            RoadLights roadLights = entry.getValue();
            List<Turn> activeTurns = roadLights.getActiveTurns();
            pollVehicles(entry.getKey(), activeTurns, leftVehicles);
        }
        advanceTrafficLights();

        return leftVehicles;
    }

    // zakładamy że istnieje tylko jeden pas
    // TODO: dla wielu pasów 
    private void pollVehicles(Direction roadDirection, List<Turn> activeTurns, List<String> leftVehicles) {
        Queue<Vehicle> queue = roads.get(roadDirection);
        
        if (queue == null || queue.isEmpty() || activeTurns == null || activeTurns.isEmpty()) {
            return; 
        }

        Vehicle vehicle = queue.peek(); 
        Turn intendedTurn = calculateTurn(roadDirection, vehicle.endRoad());

        if (activeTurns.contains(intendedTurn)) {
            queue.poll(); 
            leftVehicles.add(vehicle.id());
        }
    }

    // TODO: zliczanie faz
    private void advanceTrafficLights() {
        stepsNumber++;
        
        int timeInCurrentPhase = stepsNumber - lastPhaseChangeStep;
        boolean shouldSwitchEarly = false;
        
        boolean nsGreen = isMainLightGreen(Direction.NORTH) || isMainLightGreen(Direction.SOUTH);
        boolean ewGreen = isMainLightGreen(Direction.EAST) || isMainLightGreen(Direction.WEST);

        if (nsGreen && timeInCurrentPhase >= 2) { 
            if (isRoadEmpty(Direction.NORTH) && isRoadEmpty(Direction.SOUTH) && 
               (!isRoadEmpty(Direction.EAST) || !isRoadEmpty(Direction.WEST))) {
                shouldSwitchEarly = true;
            }
        } else if (ewGreen && timeInCurrentPhase >= 2) {
            if (isRoadEmpty(Direction.EAST) && isRoadEmpty(Direction.WEST) && 
               (!isRoadEmpty(Direction.NORTH) || !isRoadEmpty(Direction.SOUTH))) {
                shouldSwitchEarly = true;
            }
        }

        if (shouldSwitchEarly) {
            forceNextPhaseForActiveLights();
            lastPhaseChangeStep = stepsNumber;
            
        } else {
            nextStepForAll(); 
        }
    }

    private boolean isMainLightGreen(Direction direction) {
        RoadLights lights = roadsLights.get(direction);
        if (lights != null && lights.getMainLight() != null) {
            return lights.getMainLight().getState() == LightState.GREEN;
        }
        return false;
    }

    private void forceNextPhaseForActiveLights() {
        for (RoadLights lights : roadsLights.values()) {
            if (lights != null && lights.getMainLight() != null) {
                // Jeśli światło jest zielone, wymuś zmianę
                if (lights.getMainLight().getState() == LightState.GREEN) {
                    lights.getMainLight().nextPhase();
                }
            }
        }
    }


     private boolean isRoadEmpty(Direction roadDirection) {
        return roads.get(roadDirection).isEmpty();
    }

    private Turn calculateTurn(Direction start, Direction end) {
        // TODO: adding turning around
        if (start == end) {
            throw new IllegalArgumentException("Start and end direction cannot be the same!");
        }
        
        return switch (start) {
            case NORTH -> end == Direction.SOUTH ? Turn.STRAIGHT : (end == Direction.EAST ? Turn.LEFT : Turn.RIGHT);
            case SOUTH -> end == Direction.NORTH ? Turn.STRAIGHT : (end == Direction.WEST ? Turn.LEFT : Turn.RIGHT);
            case EAST  -> end == Direction.WEST  ? Turn.STRAIGHT : (end == Direction.SOUTH ? Turn.LEFT : Turn.RIGHT);
            case WEST  -> end == Direction.EAST  ? Turn.STRAIGHT : (end == Direction.NORTH ? Turn.LEFT : Turn.RIGHT);
        };
    }


}
