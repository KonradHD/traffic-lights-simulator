package com.traffic_lights.intersection;

import com.traffic_lights.intersection.phase.IntersectionPhase;
import com.traffic_lights.intersection.phase.PhaseScheduler;
import com.traffic_lights.model.Direction;
import com.traffic_lights.model.Lane;
import com.traffic_lights.model.Turn;
import com.traffic_lights.config.IntersectionConfig;
import com.traffic_lights.model.Vehicle;
import com.traffic_lights.dto.intersection.IntersectionLayout;
import com.traffic_lights.dto.intersection.LaneDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Represents a complex traffic intersection where each approach may contain multiple physical lanes.
 * <p>
 * Unlike a single-lane intersection, this implementation manages independent vehicle queues
 * for each lane. It includes logic for:
 * <ul>
 * <li><b>Lane Routing:</b> automatically directing incoming vehicles to the shortest valid lane
 * for their intended turn.</li>
 * <li><b>Conflict Resolution:</b> implementing right-of-way yielding logic e.g. unprotected left turns
 * yielding to oncoming straight traffic, or conditional right turns.</li>
 * </ul>
 * </p>
 */
@Slf4j
public class MultiLaneIntersection extends Intersection {

    /** * The physical topology of the intersection, mapping approach directions to their
     * respective lists of independent physical lanes.
     */
    private final Map<Direction, List<Lane>> roads = new HashMap<>();

    /**
     * Constructs a multi-lane intersection and initializes its physical layout.
     *
     * @param type The specific configuration identifier.
     * @param phases The list of traffic phases defining the green-light cycles.
     * @param scheduler The scheduling algorithm used to manage phase transitions.
     */
    public MultiLaneIntersection(String type, List<IntersectionPhase> phases, PhaseScheduler scheduler) {
        super(type, phases, scheduler);
        initLanes(type);
        activateCurrentPhase();

        log.info("Created {} intersection", this.intersectionType);
    }

    /**
     * Parses the global JSON configuration and builds the physical lane objects for this intersection.
     * <p>
     * Translates Data Transfer Objects ({@link LaneDTO}) into functioning domain {@link Lane}
     * objects, maintaining the ordered list of lanes for each cardinal approach.
     * </p>
     *
     * @param type The intersection type used to look up the layout template.
     */
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

    /**
     * Synchronizes the physical traffic lights of all lanes with the currently active phase.
     * <p>
     * Iterates through every lane and applies the list of permitted turns for the active
     * phase, instructing the lane to turn its relevant lights GREEN and all others RED.
     * </p>
     */
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

    /**
     * Receives a new vehicle and assigns it to the optimal physical lane.
     * <p>
     * <b>Algorithm:</b>
     * <ol>
     * <li>Identifies all lanes on the vehicle's starting road that permit its intended turn.</li>
     * <li>Finds the lane with the shortest current queue.</li>
     * <li>Enqueues the vehicle in the selected lane.</li>
     * </ol>
     * </p>
     *
     * @param vehicle The {@link Vehicle} attempting to enter the intersection.
     * @throws IllegalArgumentException if the starting direction is not present in the layout.
     * @throws IllegalStateException if the intersection layout has no lane supporting the intended turn.
     */
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

    /**
     * Determines whether an opposing or cross-traffic stream has right-of-way over a specific maneuver.
     * <p>
     * This collision-avoidance logic handles:
     * <ul>
     * <li><b>Unprotected Left Turns:</b> Must yield to oncoming traffic moving straight or turning right.</li>
     * <li><b>Conditional Right Turns:</b> Must yield to traffic moving straight from the left intersecting road.</li>
     * </ul>
     * </p>
     *
     * @param endDirection The current direction of the vehicle checking for priority.
     * @param phase The currently active traffic phase.
     * @param rightArrow {@code true} if evaluating a conditional right turn, {@code false} for an unprotected left.
     * @return {@code true} if there is conflicting traffic that holds priority (vehicle must yield),
     * {@code false} if the intersection is clear for the maneuver.
     */
    @Override
    protected boolean isSubordinate(Direction endDirection, IntersectionPhase phase, boolean rightArrow) {
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

    /**
     * Calculates the total number of queued vehicles that intend to make maneuvers
     * permitted by the specified phase.
     *
     * @param phase The {@link IntersectionPhase} to evaluate.
     * @return The count of vehicles waiting for this phase's green lights.
     */
    @Override
    public int getVehiclesForPhase(IntersectionPhase phase){
        int vehiclesCount = 0;

        for (Map.Entry<Direction, List<Lane>> entry : roads.entrySet()) {
            Direction dir = entry.getKey();
            List<Lane> lanes = entry.getValue();

            if (lanes == null || lanes.isEmpty()) {
                continue;
            }

            List<Turn> allowedTurns = phase.getTurns(dir);

            if (allowedTurns != null && !allowedTurns.isEmpty()) {
                for (Lane lane : lanes) {
                    Queue<Vehicle> queue = lane.getVehicles();

                    if (queue == null || queue.isEmpty()) {
                        continue;
                    }

                    for (Vehicle vehicle : queue) {
                        Turn intendedTurn = dir.calculateTurn(vehicle.endRoad());

                        if (allowedTurns.contains(intendedTurn)) {
                            vehiclesCount++;
                        }
                    }
                }
            }
        }
        return vehiclesCount;
    }

    /**
     * Aggregates the total number of vehicles waiting across all lanes and directions.
     *
     * @return The global queue size of the entire intersection.
     */
    @Override
    public int getVehiclesOverall(){
        int vehiclesOverall = 0;

        for (Map.Entry<Direction, List<Lane>> entry : roads.entrySet()) {
            List<Lane> lanes = entry.getValue();
            if (lanes == null || lanes.isEmpty()) {
                continue;
            }

            for (Lane lane : lanes) {
                Queue<Vehicle> queue = lane.getVehicles();
                if (queue == null || queue.isEmpty()) {
                    continue;
                }

                vehiclesOverall += queue.size();
            }
        }
        return vehiclesOverall;
    }


    /**
     * Identifies and polls vehicles from the front of the active lanes that can legally 
     * depart the intersection during the current simulation step.
     * <p>
     * The method peeks at the front vehicle of every lane authorized by the current phase.
     * If the vehicle's intended turn is allowed and it does not need to yield to prioritized 
     * traffic (via {@link #isSubordinate}), it is removed from the queue and allowed to depart.
     * </p>
     *
     * @return A list of {@link Vehicle} objects that successfully cleared the intersection in this step.
     */
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
                    if (intendedTurn == Turn.LEFT && isSubordinate(direction, currentPhase, false)) {
                        log.info("{} on lane {} is turning left and is not prioritized", vehicle.id(), lanes.indexOf(lane));
                        continue;
                    }

                    if (intendedTurn == Turn.RIGHT && isSubordinate(direction, currentPhase, true)) {
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
}
