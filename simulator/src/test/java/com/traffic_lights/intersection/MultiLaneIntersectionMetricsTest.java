package com.traffic_lights.intersection;

import com.traffic_lights.intersection.phase.IntersectionPhase;
import com.traffic_lights.model.Direction;
import com.traffic_lights.model.Lane;
import com.traffic_lights.model.Turn;
import com.traffic_lights.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.Mockito;

class MultiLaneIntersectionMetricsTest {

    private MultiLaneIntersection intersection;
    private Map<Direction, List<Lane>> fakeRoads;

    @BeforeEach
    void setUp() throws Exception {
        intersection = mock(MultiLaneIntersection.class, Mockito.CALLS_REAL_METHODS);
        fakeRoads = new HashMap<>();
        Field roadsField = MultiLaneIntersection.class.getDeclaredField("roads");
        roadsField.setAccessible(true);
        roadsField.set(intersection, fakeRoads);
    }

    private Lane createMockLane(Vehicle... vehicles) {
        Lane mockLane = mock(Lane.class);
        Queue<Vehicle> queue = new LinkedList<>(Arrays.asList(vehicles));
        when(mockLane.getVehicles()).thenReturn(queue);
        return mockLane;
    }

    @Test
    void shouldCountVehiclesOverall() {
        Lane northLane1 = createMockLane(
                new Vehicle("vehicleehicle1", Direction.NORTH, Direction.SOUTH),
                new Vehicle("vehicleehicle2", Direction.NORTH, Direction.WEST)
        );
        Lane northLane2 = createMockLane(
                new Vehicle("vehicleehicle3", Direction.NORTH, Direction.EAST)
        );
        fakeRoads.put(Direction.NORTH, List.of(northLane1, northLane2));

        Lane southLane1 = createMockLane(
                new Vehicle("vehicleehicle4", Direction.SOUTH, Direction.NORTH)
        );
        fakeRoads.put(Direction.SOUTH, List.of(southLane1));

        Lane westLaneEmpty = createMockLane();
        fakeRoads.put(Direction.WEST, List.of(westLaneEmpty));
        int overallCount = intersection.getVehiclesOverall();

        assertEquals(4, overallCount);
    }

    @Test
    void shouldReturnZeroOverall() {
        Lane laneWithNullQueue = mock(Lane.class);
        when(laneWithNullQueue.getVehicles()).thenReturn(null);

        fakeRoads.put(Direction.NORTH, null);
        fakeRoads.put(Direction.SOUTH, Collections.emptyList());
        fakeRoads.put(Direction.EAST, List.of(laneWithNullQueue));

        int overallCount = intersection.getVehiclesOverall();

        assertEquals(0, overallCount);
    }

    @Test
    void shouldCountVehiclesForPhase() {
        Vehicle northStraight = new Vehicle("vehicle_straight", Direction.NORTH, Direction.SOUTH);
        Vehicle northRight = new Vehicle("vehicle_right", Direction.NORTH, Direction.WEST);
        Vehicle northLeft = new Vehicle("vehicle_left", Direction.NORTH, Direction.EAST);

        Lane northLane = createMockLane(northStraight, northRight, northLeft);
        fakeRoads.put(Direction.NORTH, List.of(northLane));

        IntersectionPhase mockPhase = mock(IntersectionPhase.class);
        when(mockPhase.getTurns(Direction.NORTH)).thenReturn(List.of(Turn.STRAIGHT, Turn.RIGHT));
        when(mockPhase.getTurns(Direction.SOUTH)).thenReturn(Collections.emptyList());

        int countForPhase = intersection.getVehiclesForPhase(mockPhase);

        assertEquals(2, countForPhase);
    }

    @Test
    void shouldIgnoreVehicles() {
        Vehicle southVehicle = new Vehicle("vehicle_south", Direction.SOUTH, Direction.NORTH);
        Lane southLane = createMockLane(southVehicle);
        fakeRoads.put(Direction.SOUTH, List.of(southLane));

        Vehicle eastVehicle = new Vehicle("vehicle_east", Direction.EAST, Direction.WEST);
        Lane eastLane = createMockLane(eastVehicle);
        fakeRoads.put(Direction.EAST, List.of(eastLane));

        IntersectionPhase mockPhase = mock(IntersectionPhase.class);
        when(mockPhase.getTurns(Direction.EAST)).thenReturn(List.of(Turn.STRAIGHT));
        when(mockPhase.getTurns(Direction.SOUTH)).thenReturn(null);

        int countForPhase = intersection.getVehiclesForPhase(mockPhase);

        assertEquals(1, countForPhase);
    }

    @Test
    void shouldReturnZeroForPhase() {
        Lane validLane = createMockLane(new Vehicle("vehicle1", Direction.NORTH, Direction.SOUTH));
        fakeRoads.put(Direction.NORTH, List.of(validLane));

        IntersectionPhase mockPhase = mock(IntersectionPhase.class);
        when(mockPhase.getTurns(Direction.NORTH)).thenReturn(null);

        int countForPhase = intersection.getVehiclesForPhase(mockPhase);

        assertEquals(0, countForPhase);
    }
}

