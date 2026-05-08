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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SingleLaneIntersectionPriorityTest {

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
    void shouldYieldToOppositeStraightWhenTurningLeft() {
        Vehicle oppositeStraight = new Vehicle("vehicle1", Direction.EAST, Direction.WEST);
        fakeRoads.put(Direction.EAST, createMockLane(oppositeStraight));
        when(mockPhase.getTurns(Direction.EAST)).thenReturn(List.of(Turn.STRAIGHT));
        boolean isSubordinate = intersection.isSubordinate(Direction.WEST, mockPhase, false);

        assertTrue(isSubordinate);
    }

    @Test
    void shouldYieldToOppositeRightWhenTurningLeft() {
        Vehicle oppositeRight = new Vehicle("vehicle2", Direction.EAST, Direction.NORTH);
        fakeRoads.put(Direction.EAST, createMockLane(oppositeRight));
        when(mockPhase.getTurns(Direction.EAST)).thenReturn(List.of(Turn.RIGHT));
        boolean isSubordinate = intersection.isSubordinate(Direction.WEST, mockPhase, false);

        assertTrue(isSubordinate);
    }


    @Test
    void shouldNotYieldToLeftTurningWhenTurningOnRightArrow() {
        Vehicle perpendicularLeft = new Vehicle("vehicle5", Direction.NORTH, Direction.EAST);
        fakeRoads.put(Direction.NORTH, createMockLane(perpendicularLeft));
        when(mockPhase.getTurns(Direction.NORTH)).thenReturn(List.of(Turn.LEFT));
        boolean isSubordinate = intersection.isSubordinate(Direction.EAST, mockPhase, true);

        assertFalse(isSubordinate);
    }

    @Test
    void shouldReturnFalseWhenQueueIsEmpty() {
        fakeRoads.put(Direction.EAST, createMockLane());
        when(mockPhase.getTurns(Direction.EAST)).thenReturn(List.of(Turn.STRAIGHT));
        boolean isSubordinate = intersection.isSubordinate(Direction.WEST, mockPhase, false);

        assertFalse(isSubordinate);
    }

    @Test
    void shouldReturnFalseWhenNoAllowedTurnsForOpposite() {
        Vehicle oppositeStraight = new Vehicle("vehicle6", Direction.EAST, Direction.WEST);
        fakeRoads.put(Direction.EAST, createMockLane(oppositeStraight));
        when(mockPhase.getTurns(Direction.EAST)).thenReturn(null);
        boolean isSubordinate = intersection.isSubordinate(Direction.WEST, mockPhase, false);

        assertFalse(isSubordinate);
    }

}
