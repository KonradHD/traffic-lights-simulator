package com.traffic_lights.model;

import com.traffic_lights.dto.intersection.TrafficLightDTO;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrafficLightTest {

    @Test
    void shouldReturnEmptyGreenLights() {
        TrafficLight light = new TrafficLight(LightState.GREEN, TurnGroup.RIGHT_ONLY, LightType.GENERAL);
        light.nextLight();

        var result = light.greenLightTurns();

        assertTrue(result.isEmpty());
        assertEquals(LightState.ORANGE, light.getLightState());
    }

    @Test
    void shouldAllowRightTurnWhileStraightIsRed() {
        TrafficLight straightLight = new TrafficLight(LightState.RED, TurnGroup.STRAIGHT_ONLY, LightType.DIRECTIONAL);
        TrafficLight conditionalArrow = new TrafficLight(LightState.GREEN, TurnGroup.RIGHT_ONLY, LightType.CONDITIONAL);

        Lane lane = new Lane(
                List.of(Turn.STRAIGHT, Turn.RIGHT),
                List.of(straightLight, conditionalArrow)
        );

        boolean canTurnRight = lane.isLightGreen(Turn.RIGHT);
        boolean canGoStraight = lane.isLightGreen(Turn.STRAIGHT);

        assertTrue(canTurnRight);
        assertFalse(canGoStraight);
    }

    @Test
    void shouldReturnTrueWhenLightIsGreen() {
        TrafficLight light = new TrafficLight(LightState.GREEN, TurnGroup.STRAIGHT_ONLY, LightType.GENERAL);
        boolean isGreen = light.isGreen();

        assertTrue(isGreen);
    }

    @Test
    void shouldCreateTrafficLightFromDTO() {
        TrafficLightDTO dto = new TrafficLightDTO(LightState.RED, TurnGroup.RIGHT_ONLY, LightType.CONDITIONAL);
        TrafficLight light = TrafficLight.createTrafficLightFromDTO(dto);

        assertEquals(LightState.RED, light.getLightState());
        assertEquals(TurnGroup.RIGHT_ONLY, light.getTurnGroup());
        assertEquals(LightType.CONDITIONAL, light.getLightType());
    }
}
