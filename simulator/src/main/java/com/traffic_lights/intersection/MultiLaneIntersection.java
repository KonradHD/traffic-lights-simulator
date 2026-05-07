package com.traffic_lights.intersection;

import com.traffic_lights.model.Direction;
import com.traffic_lights.model.Lane;
import com.traffic_lights.model.Turn;
import com.traffic_lights.config.IntersectionConfig;
import com.traffic_lights.dto.Vehicle;
import com.traffic_lights.dto.intersection.IntersectionLayout;
import com.traffic_lights.dto.intersection.LaneDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class MultiLaneIntersection extends Intersection {

    private final Map<Direction, List<Lane>> roads = new HashMap<>();


    public MultiLaneIntersection(String type) {
        super(type);
        initLanes(type);
        activateCurrentPhase();

        log.info("Created {} intersection", this.intersectionType);
    }

    private void initLanes(String type){
        IntersectionConfig.loadConfig();
        IntersectionLayout layoutTemplate = IntersectionConfig.getLayoutForType(type.toUpperCase());

        for (Map.Entry<Direction, List<LaneDTO>> entry : layoutTemplate.roads().entrySet()) {
            Direction direction = entry.getKey();
            List<LaneDTO> jsonLanes = entry.getValue();

            List<Lane> physicalLanes = new ArrayList<>();
            for (LaneDTO jsonLane : jsonLanes) {
                physicalLanes.add(Lane.createLaneFromDTO(jsonLane));
            }

            roads.put(direction, physicalLanes);
        }
    }

    @Override
    protected void activateCurrentPhase() {
        if(phases.isEmpty()){
            log.warn("Cannot initialize lights: phases list is empty!");
            return;
        }

        IntersectionPhase currentPhase = phases.get(currentPhaseIndex);

        for (Map.Entry<Direction, List<Lane>> entry : roads.entrySet()) {
            Direction direction = entry.getKey();
            List<Lane> lanes = entry.getValue();

            List<Turn> allowedTurns = currentPhase.getTurns(direction);
            for(Lane lane : lanes){
                lane.applyCurrentPhase(allowedTurns);
            }

        }
    }


    public void addVehicleToQueue(Vehicle vehicle) {
        Direction startDirection = vehicle.startRoad();
        List<Lane> lanes = roads.get(startDirection);
        if (lanes == null) {
            throw new IllegalArgumentException("Unknown direction: " + startDirection);
        }
        Turn intendedTurn = startDirection.calculateTurn(vehicle.endRoad());

        Lane bestLane = lanes.stream()
                .filter(lane -> lane.getAvailableTurns().contains(intendedTurn))
                .min(Comparator.comparingInt(lane -> lane.getVehicles().size()))
                .orElseThrow(() -> new IllegalStateException(
                        "Not possible to turn " + intendedTurn + " from " + startDirection
                ));

        bestLane.getVehicles().add(vehicle);
        this.stats.increaseVehiclesWaiting(startDirection);
    }

    @Override
    protected boolean isPrioritized(Direction endDirection, IntersectionPhase phase, boolean rightArrow) {
        Direction startingDirection;
        if (rightArrow) {
            startingDirection = endDirection.getLeftDirection();
        } else {
            startingDirection = endDirection.getOpposite();
        }

        List<Lane> lanes = roads.get(startingDirection);
        if (lanes == null || lanes.isEmpty()) {
            return false;
        }

        List<Turn> availableTurns = phase.getTurns(startingDirection);
        if (availableTurns == null || availableTurns.isEmpty()) {
            return false;
        }

        for (Lane lane : lanes) {
            Queue<Vehicle> queue = lane.getVehicles();
            if (queue.isEmpty()) {
                continue;
            }
            Vehicle vehicle = queue.peek();
            Turn intendedTurn = startingDirection.calculateTurn(vehicle.endRoad());

            if (availableTurns.contains(intendedTurn)) {
                if (rightArrow) {
                    if (intendedTurn == Turn.STRAIGHT) {
                        return true;
                    }
                } else {
                    if (intendedTurn == Turn.STRAIGHT || intendedTurn == Turn.RIGHT) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected List<Vehicle> findVehiclesForCurrentPhase() {
        IntersectionPhase currentPhase = phases.get(currentPhaseIndex);
        log.info("Looking for new vehicles in phase {}", currentPhaseIndex);

        List<Vehicle> leftVehicles = new ArrayList<>();
        List<Direction> dirs = currentPhase.getDirections();

        for (Direction direction : dirs) {
            List<Lane> lanes = roads.get(direction);
            if (lanes == null || lanes.isEmpty()) {
                continue;
            }

            List<Turn> availableTurns = currentPhase.getTurns(direction);
            if (availableTurns == null || availableTurns.isEmpty()) {
                continue;
            }

            for (Lane lane : lanes) {
                Queue<Vehicle> queue = lane.getVehicles();
                if (queue.isEmpty()) {
                    continue;
                }

                Vehicle vehicle = queue.peek();
                Turn intendedTurn = direction.calculateTurn(vehicle.endRoad());

                if (intendedTurn != null && availableTurns.contains(intendedTurn)) {
                    if (intendedTurn == Turn.LEFT && isPrioritized(direction, currentPhase, false)) {
                        log.info("{} on lane {} is turning left and is not prioritized", vehicle.id(), lanes.indexOf(lane));
                        continue;
                    }

                    if (intendedTurn == Turn.RIGHT && isPrioritized(direction, currentPhase, true)) {
                        log.info("{} on lane {} is turning right with conditional arrow and is not prioritized", vehicle.id(), lanes.indexOf(lane));
                        continue;
                    }

                    queue.poll();
                    leftVehicles.add(vehicle);
                }
            }
        }
        return leftVehicles;
    }

    @Override
    protected int countPotentialVehiclesForPhase(IntersectionPhase phase) {
        int potentialCount = 0;

        for (Direction direction : phase.getDirections()) {
            List<Lane> lanes = roads.get(direction);
            if (lanes == null || lanes.isEmpty()) continue;

            List<Turn> allowedTurns = phase.getTurns(direction);
            if (allowedTurns == null || allowedTurns.isEmpty()) continue;

            for (Lane lane : lanes) {
                Queue<Vehicle> queue = lane.getVehicles();
                if (queue.isEmpty()) continue;

                Vehicle firstCar = queue.peek();
                Turn intendedTurn = direction.calculateTurn(firstCar.endRoad());

                if (allowedTurns.contains(intendedTurn)) {
                    boolean canActuallyGo = true;

                    if (intendedTurn == Turn.LEFT && isPrioritized(direction, phase, false)) {
                        canActuallyGo = false;
                    } else if (intendedTurn == Turn.RIGHT && isPrioritized(direction, phase, true)) {
                        canActuallyGo = false;
                    }

                    if (canActuallyGo) {
                        potentialCount++;
                    }
                }
            }
        }
        return potentialCount;
    }
}
