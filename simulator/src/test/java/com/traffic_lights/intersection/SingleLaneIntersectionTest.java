package com.traffic_lights.intersection;

import com.traffic_lights.config.IntersectionConfig;
import com.traffic_lights.dto.intersection.IntersectionParameters;
import com.traffic_lights.intersection.phase.HybridPhaseScheduler;
import com.traffic_lights.intersection.phase.IntersectionPhase;
import com.traffic_lights.intersection.phase.PhaseScheduler;
import com.traffic_lights.model.Vehicle;
import com.traffic_lights.model.Direction;
import com.traffic_lights.model.Lane;
import com.traffic_lights.model.Turn;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class SingleLaneIntersectionTest {

    private static final String TEST_DIR = "data/test_config/";
    private static final String TEST_CONFIG_FILE = TEST_DIR + "single_lane_config.json";
    private static final String INTERSECTION_TYPE = "STANDARD";
    private final List<IntersectionPhase> dummyPhases = List.of(
            new IntersectionPhase(Map.of(
                    Direction.NORTH, List.of(Turn.STRAIGHT, Turn.RIGHT),
                    Direction.SOUTH, List.of(Turn.STRAIGHT, Turn.RIGHT)), 5, 5, 0),
            new IntersectionPhase(Map.of(
                    Direction.NORTH, List.of(Turn.LEFT),
                    Direction.SOUTH, List.of(Turn.STRAIGHT)
            ), 5, 5, 0));
    private final PhaseScheduler dummyScheduler = new HybridPhaseScheduler(2, 2);

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(Paths.get(TEST_DIR));

        String fakeConfigJson = """
                {
                  "intersectionTypes": {
                    "STANDARD": {
                      "roads": {
                        "NORTH": [{"allowedTurns": ["STRAIGHT", "LEFT", "RIGHT"], "trafficLights": []}],
                        "SOUTH": [{"allowedTurns": ["STRAIGHT", "LEFT", "RIGHT"], "trafficLights": []}],
                        "EAST":  [{"allowedTurns": ["STRAIGHT", "LEFT", "RIGHT"], "trafficLights": []}],
                        "WEST":  [{"allowedTurns": ["STRAIGHT"], "trafficLights": []}]
                      },
                      "phases": []
                    }
                  }
                }
                """;
        Files.writeString(Paths.get(TEST_CONFIG_FILE), fakeConfigJson);
        IntersectionConfig.setConfigPath(TEST_CONFIG_FILE);
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(Paths.get(TEST_CONFIG_FILE));

        Field configField = IntersectionConfig.class.getDeclaredField("configuration");
        configField.setAccessible(true);
        configField.set(null, null);
    }

    @SuppressWarnings("unchecked")
    private Map<Direction, Lane> getRoads(SingleLaneIntersection intersection) throws Exception {
        Field roadsField = SingleLaneIntersection.class.getDeclaredField("roads");
        roadsField.setAccessible(true);
        return (Map<Direction, Lane>) roadsField.get(intersection);
    }


    @Test
    void shouldInitializeRoadsCorrectly() throws Exception {
        SingleLaneIntersection intersection = new SingleLaneIntersection(INTERSECTION_TYPE, dummyPhases, dummyScheduler);

        Field roadsField = SingleLaneIntersection.class.getDeclaredField("roads");
        roadsField.setAccessible(true);
        Map<Direction, Lane> roads = getRoads(intersection);

        assertEquals(4, roads.size());
        assertTrue(roads.containsKey(Direction.NORTH));
        assertTrue(roads.containsKey(Direction.WEST));
        assertTrue(roads.get(Direction.WEST).canHandleTurn(Turn.STRAIGHT));
        assertFalse(roads.get(Direction.WEST).canHandleTurn(Turn.RIGHT));
    }

    @Test
    void shouldAddVehicleToQueue() throws Exception {
        SingleLaneIntersection intersection = new SingleLaneIntersection(INTERSECTION_TYPE, dummyPhases, dummyScheduler);
        Vehicle vehicle = new Vehicle("vehicle1", Direction.NORTH, Direction.SOUTH);

        intersection.addVehicleToQueue(vehicle);

        Field roadsField = SingleLaneIntersection.class.getDeclaredField("roads");
        roadsField.setAccessible(true);
        Map<Direction, Lane> roads = getRoads(intersection);

        Queue<Vehicle> northQueue = roads.get(Direction.NORTH).getVehicles();
        Vehicle northVehicle = northQueue.peek();

        assertNotNull(northVehicle);
        assertEquals(1, northQueue.size());
        assertEquals("vehicle1", northVehicle.id());
        assertEquals(1, intersection.getStats().getVehiclesWaitingNumber());
    }


    @Test
    void shouldCountPotentialVehicles() {
        SingleLaneIntersection intersection = new SingleLaneIntersection(INTERSECTION_TYPE, dummyPhases, dummyScheduler);

        intersection.addVehicleToQueue(new Vehicle("V1", Direction.NORTH, Direction.SOUTH));
        intersection.addVehicleToQueue(new Vehicle("V2", Direction.SOUTH, Direction.NORTH));
        intersection.addVehicleToQueue(new Vehicle("V3", Direction.WEST, Direction.EAST));

        intersection.switchToPhase(0);
        IntersectionPhase currentPhase = intersection.phases.get(intersection.currentPhaseIndex);

        int potentialCount = intersection.getVehiclesForPhase(currentPhase);

        assertEquals(2, potentialCount);
    }

    @Test
    void shouldLetVehiclesPassWhenNoConflict() {
        SingleLaneIntersection intersection = new SingleLaneIntersection(INTERSECTION_TYPE, dummyPhases, dummyScheduler);
        Vehicle vehicle1 = new Vehicle("vehicle1", Direction.NORTH, Direction.SOUTH);
        Vehicle vehicle2 = new Vehicle("vehicle2", Direction.SOUTH, Direction.NORTH);

        intersection.addVehicleToQueue(vehicle1);
        intersection.addVehicleToQueue(vehicle2);

        intersection.switchToPhase(0);

        List<Vehicle> leftVehicles = intersection.findVehiclesForCurrentPhase();

        assertEquals(2, leftVehicles.size());
        assertTrue(leftVehicles.contains(vehicle1));
        assertTrue(leftVehicles.contains(vehicle2));
    }
}

