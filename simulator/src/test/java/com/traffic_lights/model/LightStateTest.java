package com.traffic_lights.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LightStateTest {

    @ParameterizedTest
    @CsvSource({
            "RED, GREEN",
            "GREEN, ORANGE",
            "ORANGE, RED"
    })
    void shouldReturnCorrectNextState(LightState currentState, LightState expectedNextState) {
        LightState actualNextState = currentState.nextState();
        assertEquals(
                expectedNextState,
                actualNextState,
                "After the state " + currentState + " should be " + expectedNextState
        );
    }
}
