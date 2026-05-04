package com.traffic_lights.components;

public enum Direction {
    SOUTH,
    WEST,
    NORTH,
    EAST;

    public Direction getOpposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }

    public Direction getDestinationDirection(Turn turn) {
        return switch (this) {
            case NORTH -> switch (turn) {
                case STRAIGHT -> SOUTH;
                case LEFT -> EAST;
                case RIGHT -> WEST;
            };
            case SOUTH -> switch (turn) {
                case STRAIGHT -> NORTH;
                case LEFT -> WEST;
                case RIGHT -> EAST;
            };
            case EAST -> switch (turn) {
                case STRAIGHT -> WEST;
                case LEFT -> SOUTH;
                case RIGHT -> NORTH;
            };
            case WEST -> switch (turn) {
                case STRAIGHT -> EAST;
                case LEFT -> NORTH;
                case RIGHT -> SOUTH;
            };
        };
    }
}
