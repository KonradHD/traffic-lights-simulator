package com.traffic_lights.intersection;


import com.traffic_lights.config.IntersectionConfig;
import com.traffic_lights.dto.Vehicle;
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

import static org.junit.jupiter.api.Assertions.*;

class MultiLaneIntersectionTest {

    private static final String TEST_DIR = "data/test_config/";
    private static final String TEST_CONFIG_FILE = TEST_DIR + "multi_lane_config.json";
    private static final String INTERSECTION_TYPE = "TEST_MULTI_LANE";

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(Paths.get(TEST_DIR));

        String fakeConfigJson = """
                {
                  "intersectionTypes": {
                    "TEST_MULTI_LANE": {
                      "roads": {
                        "NORTH": [
                          {"allowedTurns": ["STRAIGHT", "RIGHT"], "trafficLights": []},
                          {"allowedTurns": ["STRAIGHT", "LEFT"], "trafficLights": []}
                        ],
                        "SOUTH": [
                          {"allowedTurns": ["STRAIGHT", "RIGHT"], "trafficLights": []}
                        ]
                      },
                      "phases": [
                        {
                          "paths": {
                            "NORTH": ["STRAIGHT", "RIGHT", "LEFT"],
                            "SOUTH": ["STRAIGHT"]
                          },
                          "basicDuration": 5
                        }
                      ]
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
    private Map<Direction, List<Lane>> getRoads(MultiLaneIntersection intersection) throws Exception {
        Field roadsField = MultiLaneIntersection.class.getDeclaredField("roads");
        roadsField.setAccessible(true);
        return (Map<Direction, List<Lane>>) roadsField.get(intersection);
    }


    @Test
    void shouldInitializeMultipleLanesCorrectly() throws Exception {
        MultiLaneIntersection intersection = new MultiLaneIntersection(INTERSECTION_TYPE);
        Map<Direction, List<Lane>> roads = getRoads(intersection);

        assertTrue(roads.containsKey(Direction.NORTH));
        assertEquals(2, roads.get(Direction.NORTH).size());

        assertTrue(roads.containsKey(Direction.SOUTH));
        assertEquals(1, roads.get(Direction.SOUTH).size());
        assertFalse(roads.get(Direction.SOUTH).getFirst().canHandleTurn(Turn.LEFT));
    }

    @Test
    void shouldDistributeVehiclesToLanesWithShortestQueue() throws Exception {
        MultiLaneIntersection intersection = new MultiLaneIntersection(INTERSECTION_TYPE);

        Vehicle v1 = new Vehicle("vehicle1", Direction.NORTH, Direction.SOUTH);
        Vehicle v2 = new Vehicle("vehicle1", Direction.NORTH, Direction.SOUTH);
        Vehicle v3 = new Vehicle("vehicle1", Direction.NORTH, Direction.SOUTH);
        intersection.addVehicleToQueue(v1);
        intersection.addVehicleToQueue(v2);
        intersection.addVehicleToQueue(v3);

        List<Lane> northLanes = getRoads(intersection).get(Direction.NORTH);

        int firstLaneSize = northLanes.get(0).getVehicles().size();
        int secondLaneSize = northLanes.get(1).getVehicles().size();

        assertTrue(firstLaneSize > 0 && secondLaneSize > 0);
        assertEquals(3, firstLaneSize + secondLaneSize);
    }

    @Test
    void shouldAddVehicleToSpecificLane() throws Exception {
        MultiLaneIntersection intersection = new MultiLaneIntersection(INTERSECTION_TYPE);
        Vehicle turningLeftCar = new Vehicle("CAR_LEFT", Direction.NORTH, Direction.EAST);

        intersection.addVehicleToQueue(turningLeftCar);
        List<Lane> northLanes = getRoads(intersection).get(Direction.NORTH);

        assertEquals(0, northLanes.get(0).getVehicles().size());
        assertEquals(1, northLanes.get(1).getVehicles().size());
    }

    @Test
    void shouldThrowExceptionWhenNoLaneSupportsIntendedTurn() {
        MultiLaneIntersection intersection = new MultiLaneIntersection(INTERSECTION_TYPE);
        Vehicle illegalTurnCar = new Vehicle("CAR_BAD", Direction.SOUTH, Direction.WEST);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> intersection.addVehicleToQueue(illegalTurnCar)
        );
        assertTrue(exception.getMessage().contains("Not possible to turn LEFT from SOUTH"));
    }

    @Test
    void shouldLetVehiclesPass() {
        MultiLaneIntersection intersection = new MultiLaneIntersection(INTERSECTION_TYPE);
        intersection.switchToPhase(0);
        Vehicle vehicle1 = new Vehicle("vehicle_north1", Direction.NORTH, Direction.SOUTH);
        Vehicle vehicle2 = new Vehicle("vehicle_north2", Direction.NORTH, Direction.WEST);
        Vehicle vehicle3 = new Vehicle("vehicle_south1", Direction.SOUTH, Direction.NORTH);


        intersection.addVehicleToQueue(vehicle2);
        intersection.addVehicleToQueue(vehicle1);
        intersection.addVehicleToQueue(vehicle3);

        List<Vehicle> leftVehicles = intersection.findVehiclesForCurrentPhase();

        assertEquals(3, leftVehicles.size());
        assertTrue(leftVehicles.contains(vehicle1));
        assertTrue(leftVehicles.contains(vehicle2));
        assertTrue(leftVehicles.contains(vehicle3));
    }

    @Test
    void shouldYieldPriorityAcrossMultipleLanes() {
        MultiLaneIntersection intersection = new MultiLaneIntersection(INTERSECTION_TYPE);
        intersection.switchToPhase(0);
        Vehicle vehicle1 = new Vehicle("vehicle_left", Direction.NORTH, Direction.EAST);
        Vehicle vehicle2 = new Vehicle("vehicle_straight", Direction.SOUTH, Direction.NORTH);

        intersection.addVehicleToQueue(vehicle1);
        intersection.addVehicleToQueue(vehicle2);

        List<Vehicle> leftVehicles = intersection.findVehiclesForCurrentPhase();

        assertEquals(1, leftVehicles.size());
        assertTrue(leftVehicles.contains(vehicle2));
        assertFalse(leftVehicles.contains(vehicle1));
    }
}