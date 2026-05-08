package com.traffic_lights.model;

import com.traffic_lights.dto.intersection.TrafficLightDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents a physical traffic light signal located at an intersection.
 * <p>
 * A {@code TrafficLight} is responsible for signaling the current allowed movement
 * for a specific {@link TurnGroup}. Its behavior and priority are defined by its
 * {@link LightType} and its current {@link LightState}.
 * </p>
 */
@Getter
@AllArgsConstructor
public class TrafficLight {

    /**
     * The current operational color of the signal.
     */
    @Setter
    private LightState lightState;

    /** The collection of turns governed by this specific signal. */
    private final TurnGroup turnGroup;

    /** The functional category of the signal */
    private final LightType lightType;

    /**
     * Advances the signal to its next logical state in the traffic cycle.
     * <p>
     * This method delegates the transition logic to the {@link LightState#nextState()} method.
     * </p>
     */
    public void nextLight(){
        lightState = lightState.nextState();
    }

    /**
     * Retrieves the list of turns permitted by this signal when it is GREEN.
     *
     * @return A {@link List} of {@link Turn} maneuvers currently allowed;
     * returns an empty list if the light is not GREEN or is null.
     */
    public List<Turn> greenLightTurns() {
        if (lightState == null || lightState != LightState.GREEN) {
            return List.of();
        }

        return turnGroup.getTurns();
    }

    /**
     * Static factory method to create a domain-level {@code TrafficLight} from a
     * Data Transfer Object.
     *
     * @param dto The {@link TrafficLightDTO} containing initial state and configuration.
     * @return A fully initialized {@code TrafficLight} instance.
     */
    public static TrafficLight createTrafficLightFromDTO(TrafficLightDTO dto) {
        return new TrafficLight(
                dto.lightState(),
                dto.turnGroup(),
                dto.lightType()
        );
    }

    /**
     * Quick check to determine if the signal is currently in a GREEN state.
     *
     * @return {@code true} if the signal is GREEN, {@code false} otherwise.
     */
    public boolean isGreen() {
        return lightState == LightState.GREEN;
    }
}
