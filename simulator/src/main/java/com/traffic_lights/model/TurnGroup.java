package com.traffic_lights.model;

import lombok.Getter;

import java.util.List;

/**
 * Defines logical groupings of maneuvers governed by a single traffic signal or lane constraint.
 * <p>
 * {@code TurnGroup} acts as a mapping layer between a physical {@link TrafficLight}
 * and the specific {@link Turn} types it controls. It allows the simulation to handle
 * both dedicated signals - {@code LEFT_ONLY} and shared signals - {@code STRAIGHT_AND_RIGHT}.
 * </p>
 */
@Getter
public enum TurnGroup {
    STRAIGHT_ONLY(List.of(Turn.STRAIGHT)),
    LEFT_ONLY(List.of(Turn.LEFT)),
    RIGHT_ONLY(List.of(Turn.RIGHT)),
    STRAIGHT_AND_LEFT(List.of(Turn.STRAIGHT, Turn.LEFT)),
    STRAIGHT_AND_RIGHT(List.of(Turn.STRAIGHT, Turn.RIGHT)),
    ALL_DIRECTIONS(List.of(Turn.STRAIGHT, Turn.LEFT, Turn.RIGHT));

    private final List<Turn> turns;

    /**
     * Internal constructor for defining turn associations.
     *
     * @param turns The collection of turns belonging to the group.
     */
    TurnGroup(List<Turn> turns) {
        this.turns = turns;
    }

    /**
     * Checks if a specific maneuver is part of this signal group.
     *
     * @param turn The {@link Turn} maneuver to verify.
     * @return {@code true} if the maneuver is included in this group, {@code false} otherwise.
     */
    public boolean includes(Turn turn) {
        return turns.contains(turn);
    }
}
