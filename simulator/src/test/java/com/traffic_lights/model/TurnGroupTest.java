package com.traffic_lights.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TurnGroupTest {

    @ParameterizedTest
    @CsvSource({
            "STRAIGHT_ONLY, STRAIGHT",
            "LEFT_ONLY, LEFT",
            "RIGHT_ONLY, RIGHT",

            "STRAIGHT_AND_LEFT, STRAIGHT",
            "STRAIGHT_AND_LEFT, LEFT",

            "STRAIGHT_AND_RIGHT, STRAIGHT",
            "STRAIGHT_AND_RIGHT, RIGHT",

            "ALL_DIRECTIONS, STRAIGHT",
            "ALL_DIRECTIONS, LEFT",
            "ALL_DIRECTIONS, RIGHT"
    })
    void shouldReturnPositiveTurnIncludes(TurnGroup turnGroup, Turn turn) {
        boolean result = turnGroup.includes(turn);
        assertTrue(result);
    }

    @Test
    void shouldReturnCorrectListOfTurns() {
        assertEquals(
                List.of(Turn.STRAIGHT),
                TurnGroup.STRAIGHT_ONLY.getTurns()
        );
        assertEquals(
                List.of(Turn.STRAIGHT, Turn.LEFT),
                TurnGroup.STRAIGHT_AND_LEFT.getTurns()
        );
        assertEquals(
                List.of(Turn.STRAIGHT, Turn.LEFT, Turn.RIGHT),
                TurnGroup.ALL_DIRECTIONS.getTurns()
        );
    }
}
