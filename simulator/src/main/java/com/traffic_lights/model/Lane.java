package com.traffic_lights.model;

import com.traffic_lights.dto.intersection.LaneDTO;
import lombok.Getter;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * Represents a single physical traffic lane at an intersection approach.
 * <p>
 * This domain model manages the lifecycle and flow of vehicles within the lane.
 * It acts as a FIFO queue for waiting vehicles, enforces
 * directional constraints (which turns are permitted), and holds the specific
 * {@link TrafficLight} instances that govern movement from this lane.
 * </p>
 */
@Getter
public class Lane {

    /** The specific maneuvers - straight, left, right permitted from this lane. */
    private final List<Turn> availableTurns;

    /** The FIFO queue of vehicles currently waiting or driving in this lane. */
    private final Queue<Vehicle> vehicles = new ArrayDeque<>();

    /** The traffic light signals specifically controlling this lane. */
    private final List<TrafficLight> trafficLights;

    /**
     * Constructs a new {@code Lane} with specified constraints and physical infrastructure.
     *
     * @param allowedTurns The list of {@link Turn} maneuvers permitted from this lane.
     * @param lights       The list of {@link TrafficLight} objects controlling this lane.
     */
    public Lane(List<Turn> allowedTurns, List<TrafficLight> lights) {
        this.availableTurns = allowedTurns;
        this.trafficLights = lights;
    }

    /**
     * Enqueues a vehicle at the back of this lane.
     *
     * @param vehicle The {@link Vehicle} entering the lane.
     */
    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    /**
     * Verifies whether a specific maneuver can be legally executed from this lane.
     *
     * @param turn The intended {@link Turn} maneuver of a vehicle.
     * @return {@code true} if the turn is allowed, {@code false} otherwise.
     */
    public boolean canHandleTurn(Turn turn) {
        return availableTurns.contains(turn);
    }

    /**
     * Retrieves the specific traffic lights that govern a particular turn maneuver.
     * <p>
     *
     * @param intendedTurn The maneuver the vehicle wishes to execute.
     * @return A {@link List} of {@link TrafficLight} objects applicable to the requested turn.
     */
    public List<TrafficLight> getLightsForTurn(Turn intendedTurn) {

        return trafficLights.stream()
                .filter(light -> light.getTurnGroup().includes(intendedTurn))
                .toList();
    }

    /**
     * Checks if a vehicle intending to make a specific turn is currently allowed to proceed.
     *
     * @param intendedTurn The turn maneuver the vehicle wishes to execute.
     * @return {@code true} if at least one applicable traffic light is currently GREEN,
     * {@code false} if all applicable lights are RED.
     */
    public boolean isLightGreen(Turn intendedTurn) {
        List<TrafficLight> applicableLights = getLightsForTurn(intendedTurn);

        return applicableLights.stream()
                .anyMatch(TrafficLight::isGreen);
    }

    /**
     * Static factory method to instantiate a Domain {@code Lane} object from a Data Transfer Object.
     *
     * @param dto The {@link LaneDTO} containing the parsed configuration data.
     * @return A fully constructed {@code Lane} instance.
     */
    public static Lane createLaneFromDTO(LaneDTO dto){
        return new Lane(
                dto.allowedTurns(),
                dto.trafficLights().stream().map(TrafficLight::createTrafficLightFromDTO).toList()
        );
    }

    /**
     * Updates the internal states of the traffic lights for this lane based on the
     * currently active intersection phase.
     * <p>
     * If a light controls a turn that is included in the active {@code greenTurns} list,
     * it is set to GREEN. All other lights in this lane are explicitly forced to RED.
     * </p>
     *
     * @param greenTurns A list of {@link Turn} maneuvers that have green light
     * in the current intersection phase.
     */
    public void applyCurrentPhase(List<Turn> greenTurns) {
        if (greenTurns == null || greenTurns.isEmpty()) {
            for (TrafficLight light : trafficLights) {
                light.setLightState(LightState.RED);
            }
            return;
        }

        for (TrafficLight light : trafficLights) {
            boolean shouldBeGreen = light.getTurnGroup().getTurns().stream()
                    .anyMatch(greenTurns::contains);

            if (shouldBeGreen) {
                light.setLightState(LightState.GREEN);
            } else {
                light.setLightState(LightState.RED);
            }
        }
    }
}
