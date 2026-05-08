package com.traffic_lights.intersection;

import com.traffic_lights.intersection.phase.IntersectionPhase;
import com.traffic_lights.intersection.phase.PhaseMetricsProvider;
import com.traffic_lights.intersection.phase.PhaseScheduler;
import com.traffic_lights.model.Vehicle;
import com.traffic_lights.model.Direction;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * The foundational abstract class representing a generalized traffic intersection.
 * <p>
 * This class implements the core orchestration logic for the traffic simulation.
 * It manages the shared state (statistics, phase sequence, active phase) and utilizes
 * a {@link PhaseScheduler} to determine the timing and order of light changes.
 * </p>
 */
@Slf4j
public abstract class Intersection implements PhaseMetricsProvider {

    /** The specific configuration identifier mapping to a defined road layout. */
    @Getter
    protected String intersectionType;

    /** The telemetry and statistics tracker for this intersection. */
    @Getter
    protected IntersectionStats stats;

    /** The list of all available signal phases for this intersection. */
    protected List<IntersectionPhase> phases;

    /** The index of the currently active phase */
    protected int currentPhaseIndex;

    /** The strategy algorithm used to calculate phase priorities and durations. */
    protected PhaseScheduler scheduler;

    /**
     * Constructs the base intersection and initializes its state.
     * <p>
     * To prevent artificial synchronization artifacts in the simulation, the starting
     * phase is selected randomly.
     * </p>
     *
     * @param type The string identifier of the intersection configuration.
     * @param phases The list of initialized intersection phases.
     * @param scheduler The scheduling algorithm to manage phase transitions.
     */
    public Intersection(String type, List<IntersectionPhase> phases, PhaseScheduler scheduler) {
        this.intersectionType = type;
        this.phases = phases;
        this.scheduler = scheduler;
        this.stats = new IntersectionStats();

        if (!phases.isEmpty()) {
            this.currentPhaseIndex = ThreadLocalRandom.current().nextInt(phases.size());
            log.info("Selected random starting phase index: {}", this.currentPhaseIndex);
        }
    }

    /**
     * Executes the transition from the current phase to a new phase.
     * <p>
     * This method resets the waiting time of the new phase, calculates its dynamic
     * optimal duration using the scheduler, updates the active lights, and resets
     * the relevant statistic counters.
     * </p>
     *
     * @param index The index of the phase to switch to.
     */
    protected void switchToPhase(int index) {
        if (index >= phases.size() || index == currentPhaseIndex) {
            log.warn("Invalid phase switch attempt to index: {}", index);
            return;
        }

        this.currentPhaseIndex = index;
        IntersectionPhase currentPhase = phases.get(currentPhaseIndex);

        currentPhase.setWaitingTime(0);
        int optimalDuration = this.scheduler.calculateOptimalPhaseTime(currentPhase, this, getCycleBasicDuration());
        currentPhase.setOptimalDuration(optimalDuration);
        activateCurrentPhase();

        this.stats.increasePhaseChanges();
        this.stats.resetPhaseDuration();
        log.info("Switched to the intersection phase: {}", this.currentPhaseIndex);
    }

    /**
     * Calculates the theoretical baseline duration of a complete cycle by summing
     * the static basic durations of all phases.
     *
     * @return The total basic duration of a full cycle.
     */
    protected int getCycleBasicDuration() {
        return phases.stream()
                .mapToInt(IntersectionPhase::getBasicDuration)
                .sum();
    }

    /**
     * Delegates the decision of which phase to activate next to the internal scheduler.
     *
     * @return The list index of the calculated next phase.
     */
    protected int determineNextPhaseIndex() {
        return scheduler.determineNextPhaseIndex(phases, currentPhaseIndex, this);
    }

    /**
     * Increments the waiting time counter for all inactive phases that currently
     * have vehicles waiting in their corresponding queues.
     */
    private void increaseWaitingTime() {
        for(IntersectionPhase phase : phases){
            if(phases.indexOf(phase) == currentPhaseIndex){
                continue;
            }

            if(getVehiclesForPhase(phase) > 0){
                phase.increaseWaitingTime();
            }
        }
    }

    /**
     * Helper method to trigger a phase calculation and execute the switch.
     */
    private void performPhaseSwitch() {
        int nextPhaseIndex = determineNextPhaseIndex();
        switchToPhase(nextPhaseIndex);
    }

    /**
     * The core execution loop for a single discrete time step of the simulation.
     * <p>
     * Logic flow:
     * <ol>
     * <li>Checks if the current phase has exceeded its calculated duration.</li>
     * <li>Gathers the vehicles that can legally depart during this step.</li>
     * <li><b>Early Switch Optimization:</b> If the current phase is empty but other
     * approaches have traffic, it forces an immediate phase switch.</li>
     * <li>Updates all internal telemetry and counters.</li>
     * <li>Adjusts the spatial queue metrics based on the departed vehicles.</li>
     * </ol>
     * </p>
     *
     * @return A list of unique string IDs of the vehicles that departed the intersection during this step.
     */
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

    /**
     * Retrieves the count of vehicles waiting in lanes governed by the specified phase.
     *
     * @param phase The phase to check.
     * @return The number of queued vehicles.
     */
    @Override
    public abstract int getVehiclesForPhase(IntersectionPhase phase);

    /**
     * Retrieves the total count of all vehicles currently waiting at the intersection.
     *
     * @return The global queue size.
     */
    @Override
    public abstract int getVehiclesOverall();

    /**
     * Appends a newly spawned vehicle to the appropriate queue based on its starting road.
     *
     * @param vehicle The vehicle to enqueue.
     */
    public abstract void addVehicleToQueue(Vehicle vehicle);

    /**
     * Determines if a specific maneuver has priority over conflicting traffic streams.
     *
     * @param endDirection The destination direction of the maneuver.
     * @param phase The currently active phase.
     * @param rightArrow Whether the maneuver is governed by a conditional right-turn arrow.
     * @return {@code true} if it must yield, {@code false} if the maneuver is clear to proceed.
     */
    protected abstract boolean isSubordinate(Direction endDirection, IntersectionPhase phase, boolean rightArrow);

    /**
     * Scans the physical queues and extracts the specific vehicles that are allowed
     * to leave the intersection during the current simulation step.
     *
     * @return A list of departing {@link Vehicle} objects.
     */
    protected abstract List<Vehicle> findVehiclesForCurrentPhase();

    /**
     * Applies the logical constraints of the current active phase to the physical
     * traffic lights in the intersection, updating their states accordingly.
     */
    protected abstract void activateCurrentPhase();
}
