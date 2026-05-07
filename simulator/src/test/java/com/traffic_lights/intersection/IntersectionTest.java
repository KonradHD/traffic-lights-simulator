package com.traffic_lights.intersection;

import com.traffic_lights.config.IntersectionConfig;
import com.traffic_lights.dto.intersection.IntersectionParameters;
import com.traffic_lights.intersection.phase.HybridPhaseScheduler;
import com.traffic_lights.intersection.phase.IntersectionPhase;
import com.traffic_lights.intersection.phase.PhaseScheduler;
import com.traffic_lights.model.Vehicle;
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
    private final List<IntersectionPhase> dummyPhases = List.of(
            new IntersectionPhase(Map.of(), 5, 5, 0),
            new IntersectionPhase(Map.of(), 5, 5, 0),
            new IntersectionPhase(Map.of(), 5, 5, 0));
    private final PhaseScheduler dummyScheduler = new HybridPhaseScheduler(2, 2);


    static class TestableIntersection extends Intersection {

        boolean wasActivateCalled = false;
        List<Vehicle> vehiclesToReturn = new ArrayList<>();
        Map<Integer, Integer> potentialVehiclesPerPhaseIndex = new HashMap<>();

        public TestableIntersection(String type, List<IntersectionPhase> phases, PhaseScheduler scheduler) {
            super(type, phases, scheduler);
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
            return true;
        }


        @Override
        protected List<Vehicle> findVehiclesForCurrentPhase() {
            return vehiclesToReturn;
        }

        @Override
        public int getVehiclesForPhase(IntersectionPhase phase){
            int index = phases.indexOf(phase);
            return potentialVehiclesPerPhaseIndex.getOrDefault(index, 0);
        }

        @Override
        public int getVehiclesOverall(){
            int sum = 0;
            for(Map.Entry<Integer, Integer> entry : potentialVehiclesPerPhaseIndex.entrySet()){
                sum += entry.getValue();
            }
            return sum;
        }

    }


    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(Paths.get(TEST_DIR));

        String configJson = """
                {
                  "intersectionParameters": {
                      "weightQueue": 1.0,
                      "weightWaitTime": 2.0
                   },
                  "intersectionTypes": {
                    "TEST_TYPE": {
                      "roads": {},
                      "phases": [
                        { "paths": {"NORTH": []}, "basicDuration": 5 },
                        { "paths": {"SOUTH": []}, "basicDuration": 5 },
                        { "paths": {"WEST": []}, "basicDuration": 5 }
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
    void shouldOptimizePhase() {
        TestableIntersection intersection = new TestableIntersection("TEST_TYPE", dummyPhases, dummyScheduler);
        intersection.switchToPhase(0);

        intersection.potentialVehiclesPerPhaseIndex.put(0, 0);
        intersection.potentialVehiclesPerPhaseIndex.put(1, 2);
        intersection.potentialVehiclesPerPhaseIndex.put(2, 5);

        int index = intersection.determineNextPhaseIndex();
        intersection.switchToPhase(index);

        assertEquals(2, intersection.currentPhaseIndex);
    }

    @Test
    void shouldNotOptimizePhase() {
        TestableIntersection intersection = new TestableIntersection("TEST_TYPE", dummyPhases, dummyScheduler);
        intersection.switchToPhase(1);

        intersection.potentialVehiclesPerPhaseIndex.put(0, 0);
        intersection.potentialVehiclesPerPhaseIndex.put(1, 0);
        intersection.potentialVehiclesPerPhaseIndex.put(2, 0);

        int newIndex = intersection.determineNextPhaseIndex();
        intersection.switchToPhase(newIndex);

        assertEquals(1, intersection.currentPhaseIndex);
    }

    @Test
    void shouldForceNextPhaseWhenBasicDurationIsReached() {
        // Given
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
    void shouldUpdateStatsDuringProcessStep() {
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
}
