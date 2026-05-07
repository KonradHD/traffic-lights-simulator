package com.traffic_lights.intersection;

import com.traffic_lights.intersection.phase.IntersectionPhase;
import com.traffic_lights.model.Direction;
import com.traffic_lights.model.Lane;
import com.traffic_lights.model.Turn;
import com.traffic_lights.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SingleLaneIntersectionMetricsTest {

    private SingleLaneIntersection intersection;
    private Map<Direction, Lane> fakeRoads;
    private IntersectionPhase mockPhase;

    @BeforeEach
    void setUp() throws Exception {
        intersection = mock(SingleLaneIntersection.class, Mockito.CALLS_REAL_METHODS);
        fakeRoads = new HashMap<>();
        Field roadsField = SingleLaneIntersection.class.getDeclaredField("roads");
        roadsField.setAccessible(true);
        roadsField.set(intersection, fakeRoads);

        mockPhase = mock(IntersectionPhase.class);
    }

    private Lane createMockLane(Vehicle... vehicles) {
        Lane mockLane = mock(Lane.class);
        Queue<Vehicle> queue = new LinkedList<>(Arrays.asList(vehicles));
        when(mockLane.getVehicles()).thenReturn(queue);
        return mockLane;
    }


    @Test
    void shouldReturnZeroOverall() {
        Lane laneWithNullQueue = mock(Lane.class);
        when(laneWithNullQueue.getVehicles()).thenReturn(null);
        fakeRoads.put(Direction.NORTH, laneWithNullQueue);
        int overallCount = intersection.getVehiclesOverall();

        assertEquals(0, overallCount);
    }

    @Test
    void shouldCountVehiclesForPhase() {
        Vehicle northStraight = new Vehicle("vehicle_straight", Direction.NORTH, Direction.SOUTH);
        Vehicle northRight = new Vehicle("vehicle_right", Direction.NORTH, Direction.WEST);
        Vehicle northLeft = new Vehicle("vehicle_left", Direction.NORTH, Direction.EAST);

        Lane northLane = createMockLane(northStraight, northRight, northLeft);
        fakeRoads.put(Direction.NORTH, northLane);

        when(mockPhase.getTurns(Direction.NORTH)).thenReturn(List.of(Turn.STRAIGHT, Turn.RIGHT));
        when(mockPhase.getTurns(Direction.SOUTH)).thenReturn(Collections.emptyList());
        int countForPhase = intersection.getVehiclesForPhase(mockPhase);

        assertEquals(2, countForPhase);
    }

    @Test
    void shouldIgnoreVehiclesFromDirectionsNotIncludedInPhase() {
        Vehicle southVehicle = new Vehicle("vehicle_south", Direction.SOUTH, Direction.NORTH);
        Lane southLane = createMockLane(southVehicle);
        fakeRoads.put(Direction.SOUTH, southLane);

        Vehicle eastVehicle = new Vehicle("vehicle_east", Direction.EAST, Direction.WEST);
        Lane eastLane = createMockLane(eastVehicle);
        fakeRoads.put(Direction.EAST, eastLane);

        when(mockPhase.getTurns(Direction.EAST)).thenReturn(List.of(Turn.STRAIGHT));
        when(mockPhase.getTurns(Direction.SOUTH)).thenReturn(null);
        int countForPhase = intersection.getVehiclesForPhase(mockPhase);

        assertEquals(1, countForPhase);
    }
}
