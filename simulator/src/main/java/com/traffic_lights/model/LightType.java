package com.traffic_lights.model;

/**
 * Defines the functional categories of traffic light signals used in the simulation.
 * <p>
 * The {@code LightType} determines how the signal governs vehicle movement and whether
 * it provides absolute or partial priority for specific maneuvers.
 * </p>
 */
public enum LightType {

    /**
     * A standard circular signal that applies to all allowed movements from a lane.
     * Typically used for general flow control where multiple directions share the same signal state.
     */
    GENERAL,

    /**
     * A dedicated arrow-based signal that provides a protected phase for a specific maneuver.
     */
    DIRECTIONAL,

    /**
     * A signal that permits movement only under specific conditions, such as a
     * conditional right-turn arrow.
     */
    CONDITIONAL
}