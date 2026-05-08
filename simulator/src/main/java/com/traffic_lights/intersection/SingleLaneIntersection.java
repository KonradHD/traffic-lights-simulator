package com.traffic_lights.intersection;

import java.util.*;

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

/**
 * Represents a simplified traffic intersection where each approach direction consists
 * of exactly one physical lane.
 * <p>
 * In this topology, all allowed maneuvers are executed from a
 * single shared queue. This inherently creates a strict FIFO bottleneck:
 * if the front vehicle must yield,
 * all subsequent vehicles in that lane are blocked, regardless of their intended turn.
 * </p>
 */
@Slf4j
public class SingleLaneIntersection extends Intersection {

    /** * The physical topology of the intersection, mapping each approach direction
     * strictly to a single {@link Lane}.
     */
    private final Map<Direction, Lane> roads = new HashMap<>();

    /**
     * Constructs a single-lane intersection and initializes its physical layout.
     *
     * @param type The specific configuration identifier.
     * @param phases The list of traffic phases defining the green-light cycles.
     * @param scheduler The scheduling algorithm used to manage phase transitions.
     */
    public SingleLaneIntersection(String type, List<IntersectionPhase> phases, PhaseScheduler scheduler) {
        super(type, phases, scheduler);
        initRoads(type);
        activateCurrentPhase();

        log.info("Created {} intersection", this.intersectionType);
    }

    /**
     * Parses the global JSON configuration and builds the single physical lane for each approach.
     * <p>
     * This implementation explicitly extracts only the first lane from the
     * configured DTO list ({@code entry.getValue().getFirst()}), effectively ignoring
     * any additional lanes defined in the JSON for this intersection type.
     * </p>
     *
     * @param type The intersection type used to look up the layout template.
     */
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

    /**
     * Synchronizes the physical traffic lights of the single lanes with the currently active phase.
     * <p>
     * Applies the list of permitted turns for the active phase to the single lane
     * on each active approach, updating the red/green states of the lights accordingly.
     * </p>
     */
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

    /**
     * Receives a new vehicle and appends it to the single queue for its starting direction.
     *
     * @param vehicle The {@link Vehicle} entering the intersection approach.
     * @throws IllegalArgumentException if the starting direction does not exist in the layout map.
     */
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

    /**
     * Determines whether the vehicle at the front of a lane must yield to conflicting traffic.
     * <p>
     * Collision avoidance logic:
     * <ul>
     * <li><b>Unprotected Left Turns (rightArrow = false):</b> Must yield to oncoming vehicles
     * that are intending to go straight or turn right.</li>
     * <li><b>Conditional Right Turns (rightArrow = true):</b> Must yield to vehicles coming
     * from the left that are intending to go straight.</li>
     * </ul>
     * </p>
     *
     * @param endDirection The destination direction of the yielding vehicle.
     * @param phase The currently active traffic phase.
     * @param rightArrow {@code true} if evaluating a conditional right turn, {@code false} for an unprotected left.
     * @return {@code true} if the vehicle is subordinate and must yield (wait),
     * {@code false} if the intersection is clear for the maneuver.
     */
    @Override
    protected boolean isSubordinate(Direction endDirection, IntersectionPhase phase, boolean rightArrow) {
        Direction startingDirection;
        if(rightArrow){
            startingDirection = endDirection.getLeftDirection();
        }else{
            startingDirection = endDirection.getOpposite();
        }

        Lane lane = roads.get(startingDirection);
        if (lane == null) {
            return false;
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

    /**
     * Aggregates the total number of vehicles currently waiting across all 4 single lanes.
     *
     * @return The global queue size of the entire intersection.
     */
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

    /**
     * Calculates the total number of queued vehicles whose intended turns are permitted
     * by the specified phase.
     *
     * @param phase The {@link IntersectionPhase} to evaluate against the current queues.
     * @return The count of vehicles waiting to make a move permitted by this phase.
     */
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

    /**
     * Evaluates the front vehicle of every single lane and allows them to depart if their
     * maneuver is legal and safe during the current step.
     * <p>
     * Due to the single-lane nature, if the front vehicle is blocked, it remains in the queue and naturally blocks
     * all vehicles behind it.
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
            Queue<Vehicle> queue = roads.get(direction).getVehicles();

            if (queue == null || queue.isEmpty()) {
                continue;
            }
            List<Turn> availableTurns = currentPhase.getTurns(direction);
            Vehicle vehicle = queue.peek();
            Turn intendedTurn = direction.calculateTurn(vehicle.endRoad());
            if (intendedTurn != null && availableTurns.contains(intendedTurn)) {

                if (intendedTurn == Turn.LEFT && isSubordinate(direction, currentPhase, false)) {
                    log.info("{} is turning left and is not prioritized", vehicle.id());
                    continue;
                }

                if (intendedTurn == Turn.RIGHT && isSubordinate(direction, currentPhase, true)) {
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

