package com.traffic_lights.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Direction {

    @JsonProperty("south")
    SOUTH,

    @JsonProperty("west")
    WEST,

    @JsonProperty("north")
    NORTH,

    @JsonProperty("east")
    EAST;

    public Direction getOpposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }

    public Direction getLeftDirection() {
        return switch (this) {
            case SOUTH -> WEST;
            case EAST -> SOUTH;
            case NORTH -> EAST;
            case WEST -> NORTH;
        };
    }


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
