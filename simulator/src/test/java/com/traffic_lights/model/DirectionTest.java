package com.traffic_lights.model;


import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class DirectionTest {

    @ParameterizedTest
    @CsvSource({
            "NORTH, EAST, LEFT",
            "NORTH, SOUTH, STRAIGHT",
            "EAST, SOUTH, LEFT",
            "SOUTH, WEST, LEFT"
    })
    public void shouldCalculateProperTurns(Direction startDir, Direction endDir, Turn expectedTurn) {
        assertEquals(expectedTurn, startDir.calculateTurn(endDir));
    }


    @ParameterizedTest
    @EnumSource(Direction.class)
    void shouldThrowExceptionWhenStartAndEndDirectionAreTheSame(Direction direction) {
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> direction.calculateTurn(direction)
        );
        assertEquals("Start and end direction cannot be the same", e.getMessage());
    }


    @ParameterizedTest
    @CsvSource({
            "NORTH, SOUTH",
            "SOUTH, NORTH",
            "EAST, WEST",
            "WEST, EAST"
    })
    void shouldReturnOppositeDirection(Direction start, Direction expectedOpposite) {
        Direction actualOpposite = start.getOpposite();
        assertEquals(expectedOpposite, actualOpposite);
    }


    @ParameterizedTest
    @CsvSource({
            "SOUTH, WEST",
            "EAST, SOUTH",
            "NORTH, EAST",
            "WEST, NORTH"
    })
    void shouldReturnLeftDirection(Direction start, Direction expectedLeft) {
        Direction actualLeft = start.getLeftDirection();
        assertEquals(expectedLeft, actualLeft);
    }

}
