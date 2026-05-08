package com.traffic_lights.model;

import static com.traffic_lights.exception.UndeclaredNextStateException.undeclaredNextStateException;

/**
 * Represents the functional and operational states of a traffic light signal.
 * <p>
 * This enum defines the standard signaling colors used in the simulation,
 * as well as an exceptional state for maintenance or system failure.
 * </p>
 */
public enum LightState {
    RED,
    ORANGE,
    GREEN,
    NONFUNCTIONAL;

    /**
     * Determines the next logical state in the traffic light cycle.
     * <p>
     * The transition follows the sequence:
     * {@code RED} -> {@code GREEN} -> {@code ORANGE} -> {@code RED}.
     * </p>
     * * @return The subsequent {@link LightState} in the sequence.
     * @throws com.traffic_lights.exception.UndeclaredNextStateException if called on {@code NONFUNCTIONAL},
     * as this state does not belong to the standard operational cycle.
     * @throws IllegalStateException if an unhandled or unknown state is encountered.
     */
    public LightState nextState(){
        switch (this) {
            case NONFUNCTIONAL -> throw undeclaredNextStateException(NONFUNCTIONAL.toString());
            case RED -> {
                return GREEN;
            }
            case GREEN -> {
                return ORANGE;
            }
            case ORANGE -> {
                return RED;
            }
            default -> {
            }
        }
        throw new IllegalStateException("Unknown light state: " + this);
    }
}
