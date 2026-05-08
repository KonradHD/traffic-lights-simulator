package com.traffic_lights.intersection;

import com.traffic_lights.intersection.phase.HybridPhaseScheduler;
import com.traffic_lights.intersection.phase.IntersectionPhase;
import com.traffic_lights.intersection.phase.PhaseScheduler;
import com.traffic_lights.model.Vehicle;
import com.traffic_lights.model.Direction;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.*;

class IntersectionTest {
    private final List<IntersectionPhase> dummyPhases = List.of(
            new IntersectionPhase(Map.of(Direction.NORTH, List.of()), 5, 5, 0),
            new IntersectionPhase(Map.of(Direction.SOUTH, List.of()), 5, 5, 0),
            new IntersectionPhase(Map.of(Direction.WEST, List.of()), 5, 5, 0)
    );
    private final PhaseScheduler dummyScheduler = new HybridPhaseScheduler(1.0, 1.0);


    static class TestableIntersection extends Intersection {

        boolean wasActivateCalled = false;
        List<Vehicle> vehiclesToReturn = new ArrayList<>();
        Map<Integer, Integer> potentialVehiclesPerPhase = new HashMap<>();

        public TestableIntersection(String type, List<IntersectionPhase> phases, PhaseScheduler scheduler) {
            super(type, phases, scheduler);
        }

        @Override
        protected void activateCurrentPhase() {
            wasActivateCalled = true;
        }

        @Override
        public void addVehicleToQueue(Vehicle vehicle) {}

        @Override
        protected boolean isSubordinate(Direction endDirection, IntersectionPhase phase, boolean rightArrow) {
            return true;
        }

        @Override
        protected List<Vehicle> findVehiclesForCurrentPhase() {
            return vehiclesToReturn;
        }


        @Override
        public int getVehiclesForPhase(IntersectionPhase phase) {
            int index = phases.indexOf(phase);
            return potentialVehiclesPerPhase.getOrDefault(index, 0);
        }

        @Override
        public int getVehiclesOverall() {
            return potentialVehiclesPerPhase.values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
        }

    }

    @Test
    void shouldOptimizePhase() {
        TestableIntersection intersection = new TestableIntersection("TEST_TYPE", dummyPhases, dummyScheduler);
        intersection.switchToPhase(0);

        intersection.potentialVehiclesPerPhase.put(0, 0);
        intersection.potentialVehiclesPerPhase.put(1, 2);
        intersection.potentialVehiclesPerPhase.put(2, 5);

        int nextIndex = intersection.determineNextPhaseIndex();
        intersection.switchToPhase(nextIndex);
        
        assertEquals(2, intersection.currentPhaseIndex);
    }

    @Test
    void shouldNotOptimizePhase() {
        TestableIntersection intersection = new TestableIntersection("TEST_TYPE", dummyPhases, dummyScheduler);
        intersection.switchToPhase(1);
        
        intersection.potentialVehiclesPerPhase.put(0, 0);
        intersection.potentialVehiclesPerPhase.put(1, 0);
        intersection.potentialVehiclesPerPhase.put(2, 0);

        int nextIndex = intersection.determineNextPhaseIndex();
        intersection.switchToPhase(nextIndex);

        assertEquals(2, intersection.currentPhaseIndex);
    }

    @Test
    void shouldProcessStepAndSwitchPhase() {
        TestableIntersection intersection = new TestableIntersection("TEST_TYPE", dummyPhases, dummyScheduler);
        intersection.switchToPhase(0);
        
        intersection.getStats().increasePhaseDuration();
        intersection.getStats().increasePhaseDuration();
        intersection.getStats().increasePhaseDuration();
        intersection.getStats().increasePhaseDuration();
        intersection.getStats().increasePhaseDuration();
        
        intersection.potentialVehiclesPerPhase.put(1, 10);
        intersection.processStep();

        assertEquals(1, intersection.currentPhaseIndex);
    }

    @Test
    void shouldProcessStepAndSwitchPhase2() {
        TestableIntersection intersection = new TestableIntersection("TEST_TYPE", dummyPhases, dummyScheduler);
        intersection.switchToPhase(0);

        intersection.getStats().resetPhaseDuration();
        intersection.vehiclesToReturn = Collections.emptyList();

        intersection.getStats().addWaitingVehicles(Direction.SOUTH, 5);
        intersection.potentialVehiclesPerPhase.put(2, 5);

        intersection.processStep();

        assertEquals(2, intersection.currentPhaseIndex);
    }

    @Test
    void shouldForceNextPhaseWhenBasicDurationIsReached() {
        TestableIntersection intersection = new TestableIntersection("TEST_TYPE", dummyPhases, dummyScheduler);
        intersection.switchToPhase(0);

        intersection.getStats().increasePhaseDuration();
        intersection.getStats().increasePhaseDuration();
        intersection.getStats().increasePhaseDuration();
        intersection.getStats().increasePhaseDuration();
        intersection.getStats().increasePhaseDuration();

        intersection.processStep();

        assertEquals(1, intersection.currentPhaseIndex);
    }

    @Test
    void shouldUpdateStats() {
        TestableIntersection intersection = new TestableIntersection("TEST_TYPE", dummyPhases, dummyScheduler);
        intersection.switchToPhase(0);
        intersection.vehiclesToReturn = List.of(
                new Vehicle("vehicle1", Direction.EAST, Direction.SOUTH),
                new Vehicle("vehicle2", Direction.EAST, Direction.SOUTH));
        intersection.getStats().addWaitingVehicles(Direction.EAST, 5);

        List<String> leftVehicles = intersection.processStep();

        assertEquals(2, leftVehicles.size());
        assertEquals(2, intersection.getStats().getVehiclesLeftNumber());
        assertEquals(3, intersection.getStats().getVehiclesWaitingNumber());
        assertEquals(1, intersection.getStats().getStepsNumber());
    }

    @Test
    void shouldSwitchToNewPhase() {
        PhaseScheduler mockScheduler = Mockito.mock(PhaseScheduler.class);
        TestableIntersection intersection = new TestableIntersection("TEST_TYPE", dummyPhases, mockScheduler);
        intersection.currentPhaseIndex = 0;

        IntersectionPhase targetPhase = dummyPhases.get(1);
        targetPhase.setWaitingTime(50);
        intersection.getStats().increasePhaseDuration();

        when(mockScheduler.calculateOptimalPhaseTime(eq(targetPhase), eq(intersection), anyInt()))
                .thenReturn(42);

        intersection.switchToPhase(1);

        assertEquals(1, intersection.currentPhaseIndex);
        assertEquals(0, targetPhase.getWaitingTime());
        assertEquals(42, targetPhase.getOptimalDuration());
        assertTrue(intersection.wasActivateCalled);
        assertEquals(1, intersection.getStats().getPhaseChanges());
        assertEquals(0, intersection.getStats().getPhaseDuration());

        verify(mockScheduler, times(1)).calculateOptimalPhaseTime(eq(targetPhase), eq(intersection), anyInt());
    }

    @Test
    void shouldAbortSwitchIndexIsOutOfBounds() {
        PhaseScheduler mockScheduler = Mockito.mock(PhaseScheduler.class);
        TestableIntersection intersection = new TestableIntersection("TEST_TYPE", dummyPhases, mockScheduler);
        intersection.currentPhaseIndex = 0;
        intersection.wasActivateCalled = false;

        intersection.switchToPhase(999);

        assertEquals(0, intersection.currentPhaseIndex);
        assertFalse(intersection.wasActivateCalled);
        assertEquals(0, intersection.getStats().getPhaseChanges());

        verifyNoInteractions(mockScheduler);
    }

    @Test
    void shouldAbortSwitchIndexIsTheSameAsCurrent() {
        PhaseScheduler mockScheduler = Mockito.mock(PhaseScheduler.class);
        TestableIntersection intersection = new TestableIntersection("TEST_TYPE", dummyPhases, mockScheduler);
        intersection.currentPhaseIndex = 0;
        intersection.wasActivateCalled = false;

        intersection.switchToPhase(0);

        assertFalse(intersection.wasActivateCalled);
        assertEquals(0, intersection.getStats().getPhaseChanges());

        verifyNoInteractions(mockScheduler);
    }
}
