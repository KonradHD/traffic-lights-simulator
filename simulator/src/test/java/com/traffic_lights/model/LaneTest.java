package com.traffic_lights.model;

import com.traffic_lights.dto.Vehicle;
import com.traffic_lights.dto.intersection.LaneDTO;
import com.traffic_lights.dto.intersection.TrafficLightDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LaneTest {

    @Test
    void shouldHandleTurn() {
        Lane lane = new Lane(List.of(Turn.STRAIGHT, Turn.RIGHT), List.of());

        assertTrue(lane.canHandleTurn(Turn.STRAIGHT));
        assertTrue(lane.canHandleTurn(Turn.RIGHT));
        assertFalse(lane.canHandleTurn(Turn.LEFT));
    }


    @Test
    void shouldAddVehicleToTheQueue() {
        Lane lane = new Lane(List.of(Turn.STRAIGHT), List.of());
        Vehicle vehicle = new Vehicle("vehicle1", Direction.NORTH, Direction.SOUTH);

        lane.addVehicle(vehicle);
        Queue<Vehicle> vehicles = lane.getVehicles();

        assertEquals(1, vehicles.size());
        assertEquals(vehicle, vehicles.peek());
    }

    @Test
    void shouldReturnCorrectLightsForTurn() {
        TrafficLight mainLight = new TrafficLight(LightState.RED, TurnGroup.STRAIGHT_AND_RIGHT, LightType.GENERAL);
        TrafficLight conditionalLight = new TrafficLight(LightState.RED, TurnGroup.RIGHT_ONLY, LightType.CONDITIONAL);

        Lane lane = new Lane(List.of(Turn.STRAIGHT, Turn.RIGHT), List.of(mainLight, conditionalLight));

        List<TrafficLight> lightsForStraight = lane.getLightsForTurn(Turn.STRAIGHT);
        List<TrafficLight> lightsForRight = lane.getLightsForTurn(Turn.RIGHT);

        assertEquals(1, lightsForStraight.size());
        assertEquals(mainLight, lightsForStraight.getFirst());

        assertEquals(2, lightsForRight.size());
        assertTrue(lightsForRight.contains(mainLight));
        assertTrue(lightsForRight.contains(conditionalLight));
    }

    @Test
    void shouldCheckIfLightIsGreen() {
        TrafficLight mainLight = new TrafficLight(LightState.RED, TurnGroup.STRAIGHT_AND_RIGHT, LightType.GENERAL);
        TrafficLight conditionalLight = new TrafficLight(LightState.GREEN, TurnGroup.RIGHT_ONLY, LightType.CONDITIONAL);

        Lane lane = new Lane(List.of(Turn.STRAIGHT, Turn.RIGHT), List.of(mainLight, conditionalLight));

        assertTrue(lane.isLightGreen(Turn.RIGHT));
        assertFalse(lane.isLightGreen(Turn.STRAIGHT));
    }

    @Test
    void shouldSetAllLightsToRedWhenCurrentPhaseIsEmpty() {
        TrafficLight straightLight = new TrafficLight(LightState.GREEN, TurnGroup.STRAIGHT_ONLY, LightType.GENERAL);
        TrafficLight leftLight = new TrafficLight(LightState.GREEN, TurnGroup.LEFT_ONLY, LightType.DIRECTIONAL);
        Lane lane = new Lane(List.of(Turn.STRAIGHT, Turn.LEFT), List.of(straightLight, leftLight));

        lane.applyCurrentPhase(List.of());

        assertEquals(LightState.RED, straightLight.getLightState());
        assertEquals(LightState.RED, leftLight.getLightState());
    }

    @Test
    void shouldSetCorrectLightsOnPhase() {
        TrafficLight straightRightLight = new TrafficLight(LightState.RED, TurnGroup.STRAIGHT_AND_RIGHT, LightType.GENERAL);
        TrafficLight leftLight = new TrafficLight(LightState.RED, TurnGroup.LEFT_ONLY, LightType.DIRECTIONAL);
        Lane lane = new Lane(List.of(Turn.STRAIGHT, Turn.RIGHT, Turn.LEFT), List.of(straightRightLight, leftLight));

        lane.applyCurrentPhase(List.of(Turn.STRAIGHT));

        assertEquals(LightState.GREEN, straightRightLight.getLightState());
        assertEquals(LightState.RED, leftLight.getLightState());
    }

    @Test
    void shouldCreateLaneFromDTO() {
        TrafficLightDTO lightDTO = new TrafficLightDTO(LightState.RED, TurnGroup.STRAIGHT_ONLY, LightType.GENERAL);
        LaneDTO laneDTO = new LaneDTO(List.of(Turn.STRAIGHT), List.of(lightDTO));
        Lane lane = Lane.createLaneFromDTO(laneDTO);

        assertEquals(1, lane.getAvailableTurns().size());
        assertTrue(lane.getAvailableTurns().contains(Turn.STRAIGHT));

        assertEquals(1, lane.getTrafficLights().size());
        assertEquals(LightState.RED, lane.getTrafficLights().getFirst().getLightState());
        assertEquals(TurnGroup.STRAIGHT_ONLY, lane.getTrafficLights().getFirst().getTurnGroup());
    }
}
