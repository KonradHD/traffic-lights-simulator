package com.traffic_lights.model;

import lombok.Getter;

import java.util.List;

@Getter
public enum TurnGroup {
    STRAIGHT_ONLY(List.of(Turn.STRAIGHT)),
    LEFT_ONLY(List.of(Turn.LEFT)),
    RIGHT_ONLY(List.of(Turn.RIGHT)),
    STRAIGHT_AND_LEFT(List.of(Turn.STRAIGHT, Turn.LEFT)),
    STRAIGHT_AND_RIGHT(List.of(Turn.STRAIGHT, Turn.RIGHT)),
    ALL_DIRECTIONS(List.of(Turn.STRAIGHT, Turn.LEFT, Turn.RIGHT));

    private final List<Turn> turns;

    TurnGroup(List<Turn> turns) {
        this.turns = turns;
    }

    public boolean includes(Turn turn) {
        return turns.contains(turn);
    }
}
