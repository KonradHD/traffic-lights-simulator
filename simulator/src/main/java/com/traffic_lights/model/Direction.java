package com.traffic_lights.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the cardinal directions available for roads and vehicle movement.
 * <p>
 * This enum defines the geographical orientation of the traffic flow and provides
 * utility methods for spatial logic, such as determining opposite directions,
 * relative left turns, and calculating specific maneuvers.
 * </p>
 */
public enum Direction {

    @JsonProperty("south")
    SOUTH,

    @JsonProperty("west")
    WEST,

    @JsonProperty("north")
    NORTH,

    @JsonProperty("east")
    EAST;

    /**
     * Identifies the direction directly opposite to the current one.
     *
     * @return The {@link Direction} located 180 degrees from this one.
     */
    public Direction getOpposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }

    /**
     * Determines the direction located 90 degrees to the left of the current direction.
     * This is calculated assuming a clockwise cardinal layout.
     *
     * @return The {@link Direction} to the immediate left.
     */
    public Direction getLeftDirection() {
        return switch (this) {
            case SOUTH -> WEST;
            case EAST -> SOUTH;
            case NORTH -> EAST;
            case WEST -> NORTH;
        };
    }

    /**
     * Calculates the specific {@link Turn} required to move from this direction
     * to a specified destination direction.
     * <p>
     * Logic is based on standard intersection geometry:
     * <ul>
     * <li>Opposite directions result in {@link Turn#STRAIGHT}.</li>
     * <li>Clockwise-next directions result in {@link Turn#LEFT} (in a standard cardinal system).</li>
     * <li>Counter-clockwise directions result in {@link Turn#RIGHT}.</li>
     * </ul>
     * </p>
     *
     * @param end The destination direction of the vehicle.
     * @return The calculated {@link Turn} type.
     * @throws IllegalArgumentException if the {@code end} direction is identical to the starting direction.
     */
    public Turn calculateTurn(Direction end) {
        // TODO: adding turning around
        if (this == end) {
            throw new IllegalArgumentException("Start and end direction cannot be the same");
        }

        return switch (this) {
            case NORTH -> end == Direction.SOUTH ? Turn.STRAIGHT : (end == Direction.EAST ? Turn.LEFT : Turn.RIGHT);
            case SOUTH -> end == Direction.NORTH ? Turn.STRAIGHT : (end == Direction.WEST ? Turn.LEFT : Turn.RIGHT);
            case EAST  -> end == Direction.WEST  ? Turn.STRAIGHT : (end == Direction.SOUTH ? Turn.LEFT : Turn.RIGHT);
            case WEST  -> end == Direction.EAST  ? Turn.STRAIGHT : (end == Direction.NORTH ? Turn.LEFT : Turn.RIGHT);
        };
    }
}
