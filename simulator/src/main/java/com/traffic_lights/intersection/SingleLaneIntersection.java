package com.traffic_lights.intersection;

import java.util.*;

import com.traffic_lights.dto.intersection.IntersectionParameters;
import com.traffic_lights.intersection.phase.IntersectionPhase;
import com.traffic_lights.intersection.phase.PhaseScheduler;
import com.traffic_lights.model.*;
import com.traffic_lights.config.IntersectionConfig;
import com.traffic_lights.model.Vehicle;

import com.traffic_lights.dto.intersection.IntersectionLayout;
import com.traffic_lights.dto.intersection.LaneDTO;
import com.traffic_lights.model.Lane;
import com.traffic_lights.model.Turn;
import lombok.extern.slf4j.Slf4j;

import static com.traffic_lights.model.Lane.createLaneFromDTO;

@Slf4j
public class SingleLaneIntersection extends Intersection {

    private final Map<Direction, Lane> roads = new HashMap<>();


    public SingleLaneIntersection(String type, List<IntersectionPhase> phases, PhaseScheduler scheduler) {
        super(type, phases, scheduler);
        initRoads(type);
        activateCurrentPhase();

        log.info("Created {} intersection", this.intersectionType);
    }

    private void initRoads(String type){
        IntersectionConfig.loadConfig();
        IntersectionLayout layoutTemplate = IntersectionConfig.getLayoutForType(type.toUpperCase());

        for (Map.Entry<Direction, List<LaneDTO>> entry : layoutTemplate.roads().entrySet()) {
            Direction direction = entry.getKey();
            LaneDTO jsonLane = entry.getValue().getFirst();
            Lane lane = createLaneFromDTO(jsonLane);

            roads.put(direction, lane);
        }
    }

    @Override
    protected void activateCurrentPhase() {
        if(phases.isEmpty()){
            log.warn("Cannot initialize lights: phases list is empty!");
            return;
        }

        IntersectionPhase currentPhase = phases.get(currentPhaseIndex);

        for (Map.Entry<Direction, Lane> entry : roads.entrySet()) {
            Direction direction = entry.getKey();
            Lane lane = entry.getValue();

            List<Turn> allowedTurns = currentPhase.getTurns(direction);
            lane.applyCurrentPhase(allowedTurns);
        }
    }


    @Override
    public void addVehicleToQueue(Vehicle vehicle) {
        Direction dir = vehicle.startRoad();
        Queue<Vehicle> queue = roads.get(dir).getVehicles();
        if (queue == null) {
            throw new IllegalArgumentException("Unknown direction: " + vehicle.startRoad());
        }
        queue.add(vehicle);
        this.stats.increaseVehiclesWaiting(dir);
    }


    @Override
    protected boolean isPrioritized(Direction endDirection, IntersectionPhase phase, boolean rightArrow) {
        Direction startingDirection;
        if(rightArrow){
            startingDirection = endDirection.getLeftDirection();
        }else{
            startingDirection = endDirection.getOpposite();
        }

        Queue<Vehicle> queue = roads.get(startingDirection).getVehicles();
        if (queue == null || queue.isEmpty()) {
            return false;
        }

        Vehicle vehicle = queue.peek();
        Turn intendedTurn = startingDirection.calculateTurn(vehicle.endRoad());
        List<Turn> availableTurns = phase.getTurns(startingDirection);

        if(rightArrow){
            return availableTurns != null && availableTurns.contains(intendedTurn) && intendedTurn == Turn.STRAIGHT;
        }else{
            return availableTurns != null && availableTurns.contains(intendedTurn) && (intendedTurn == Turn.STRAIGHT || intendedTurn == Turn.RIGHT);
        }
    }


    @Override
    public int getVehiclesOverall(){
        int vehiclesOverall = 0;
        for(Map.Entry<Direction, Lane> entry : roads.entrySet()){
            Direction dir = entry.getKey();
            Queue<Vehicle> queue = roads.get(dir).getVehicles();

            if (queue == null || queue.isEmpty()) {
                continue;
            }
            vehiclesOverall += queue.size();
        }
        return vehiclesOverall;
    }

    @Override
    public int getVehiclesForPhase(IntersectionPhase phase){
        int vehiclesCount = 0;

        for (Map.Entry<Direction, Lane> entry : roads.entrySet()) {
            Direction dir = entry.getKey();
            Queue<Vehicle> queue = entry.getValue().getVehicles();

            if (queue == null || queue.isEmpty()) {
                continue;
            }

            List<Turn> allowedTurns = phase.getTurns(dir);

            if (allowedTurns != null && !allowedTurns.isEmpty()) {
                for (Vehicle vehicle : queue) {
                    Turn intendedTurn = dir.calculateTurn(vehicle.endRoad());

                    if (allowedTurns.contains(intendedTurn)) {
                        vehiclesCount++;
                    }
                }
            }
        }
        return vehiclesCount;
    }


    @Override
    protected List<Vehicle> findVehiclesForCurrentPhase() {
        IntersectionPhase currentPhase = phases.get(currentPhaseIndex);
        log.info("Looking for new vehicles in phase {}", currentPhaseIndex);

        List<Vehicle> leftVehicles = new ArrayList<>();
        List<Direction> dirs = currentPhase.getDirections();
        for (Direction direction : dirs) {
            Queue<Vehicle> queue = roads.get(direction).getVehicles();

            if (queue == null || queue.isEmpty()) {
                continue;
            }
            List<Turn> availableTurns = currentPhase.getTurns(direction);
            Vehicle vehicle = queue.peek();
            Turn intendedTurn = direction.calculateTurn(vehicle.endRoad());
            if (intendedTurn != null && availableTurns.contains(intendedTurn)) {

                if (intendedTurn == Turn.LEFT && isPrioritized(direction, currentPhase, false)) {
                    log.info("{} is turning left and is not prioritized", vehicle.id());
                    continue;
                }

                if (intendedTurn == Turn.RIGHT && isPrioritized(direction, currentPhase, true)) {
                    log.info("{} is turning right with conditional arrow and is not prioritized", vehicle.id());
                    continue;
                }

                queue.poll();
                leftVehicles.add(vehicle);
            }
        }
        return leftVehicles;
    }

}

