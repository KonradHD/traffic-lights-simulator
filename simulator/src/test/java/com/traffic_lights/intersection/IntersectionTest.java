package com.traffic_lights.intersection;

import com.traffic_lights.config.IntersectionConfig;
import com.traffic_lights.dto.Vehicle;
import com.traffic_lights.model.Direction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntersectionTest {

    private static final String TEST_DIR = "data/test_config/";
    private static final String TEST_CONFIG_FILE = TEST_DIR + "intersection_config.json";


    static class TestableIntersection extends Intersection {

        boolean wasActivateCalled = false;
        List<String> vehiclesToReturn = new ArrayList<>();
        Map<Integer, Integer> potentialVehiclesPerPhaseIndex = new HashMap<>();

        public TestableIntersection(String type) {
            super(type);
        }

        @Override
        protected void activateCurrentPhase() {
            wasActivateCalled = true;
        }

        @Override
        public void addVehicleToQueue(Vehicle vehicle) {
        }

        @Override
        protected boolean isPrioritized(Direction endDirection, IntersectionPhase phase, boolean rightArrow) {
            return false;
        }

        @Override
        protected List<String> findVehiclesForCurrentPhase() {
            return vehiclesToReturn;
        }

        @Override
        protected int countPotentialVehiclesForPhase(IntersectionPhase phase) {
            int index = phases.indexOf(phase);
            return potentialVehiclesPerPhaseIndex.getOrDefault(index, 0);
        }
    }


    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(Paths.get(TEST_DIR));

        String configJson = """
                {
                  "configs": {
                    "TEST_TYPE": {
                      "roads": {},
                      "phases": [
                        { "paths": {"NORTH": []}, "maxDuration": 5 },
                        { "paths": {"SOUTH": []}, "maxDuration": 5 },
                        { "paths": {"WEST": []}, "maxDuration": 5 }
                      ]
                    }
                  }
                }
                """;
        Files.writeString(Paths.get(TEST_CONFIG_FILE), configJson);
        IntersectionConfig.setConfigPath(TEST_CONFIG_FILE);
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(Paths.get(TEST_CONFIG_FILE));

        Field configField = IntersectionConfig.class.getDeclaredField("configuration");
        configField.setAccessible(true);
        configField.set(null, null);
    }


    @Test
    void shouldSwitchToNextPhase() {
        TestableIntersection intersection = new TestableIntersection("TEST_TYPE");
        intersection.switchToPhase(1);

        intersection.switchToNextPhase();

        assertEquals(2, intersection.currentPhaseIndex);
        assertEquals(0, intersection.getStats().getPhaseDuration());
        assertTrue(intersection.wasActivateCalled);

        intersection.switchToNextPhase();
        assertEquals(0, intersection.currentPhaseIndex);
    }

    @Test
    void shouldOptimizePhase() {
        TestableIntersection intersection = new TestableIntersection("TEST_TYPE");
        intersection.switchToPhase(0);

        intersection.potentialVehiclesPerPhaseIndex.put(0, 0);
        intersection.potentialVehiclesPerPhaseIndex.put(1, 2);
        intersection.potentialVehiclesPerPhaseIndex.put(2, 5);

        boolean wasOptimized = intersection.optimizeCurrentPhase();

        assertTrue(wasOptimized);
        assertEquals(2, intersection.currentPhaseIndex);
    }

    @Test
    void shouldNotOptimizePhase() {
        TestableIntersection intersection = new TestableIntersection("TEST_TYPE");
        intersection.switchToPhase(1);

        intersection.potentialVehiclesPerPhaseIndex.put(0, 0);
        intersection.potentialVehiclesPerPhaseIndex.put(1, 0);
        intersection.potentialVehiclesPerPhaseIndex.put(2, 0);

        boolean wasOptimized = intersection.optimizeCurrentPhase();

        assertFalse(wasOptimized);
        assertEquals(1, intersection.currentPhaseIndex);
    }

    @Test
    void shouldForceNextPhaseWhenMaxDurationIsReached() {
        // Given
        TestableIntersection intersection = new TestableIntersection("TEST_TYPE");
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
    void shouldUpdateStatsDuringProcessStep() {
        TestableIntersection intersection = new TestableIntersection("TEST_TYPE");
        intersection.switchToPhase(0);
        intersection.vehiclesToReturn = List.of("vehicle1", "vehicle2");
        intersection.getStats().addWaitingVehicles(5);

        List<String> leftVehicles = intersection.processStep();

        assertEquals(2, leftVehicles.size());
        assertEquals(2, intersection.getStats().getVehiclesLeftNumber());
        assertEquals(3, intersection.getStats().getVehiclesWaitingNumber());
        assertEquals(1, intersection.getStats().getStepsNumber());
    }
}
