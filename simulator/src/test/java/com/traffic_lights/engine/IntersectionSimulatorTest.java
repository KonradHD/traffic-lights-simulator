package com.traffic_lights.engine;

import com.traffic_lights.commands.Command;
import com.traffic_lights.commands.StepCommand;
import com.traffic_lights.config.IntersectionConfig;
import com.traffic_lights.dto.SimulationOutput;
import com.traffic_lights.intersection.Intersection;
import com.traffic_lights.intersection.MultiLaneIntersection;
import com.traffic_lights.intersection.SingleLaneIntersection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IntersectionSimulatorTest {

    private final ArgumentCaptor<Intersection> intersectionCaptor = ArgumentCaptor.forClass(Intersection.class);
    private static final String TEST_CONFIG_DIR = "data/test_config/";
    private static final String TEST_CONFIG_FILE = TEST_CONFIG_DIR + "test_config.json";

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(Paths.get(TEST_CONFIG_DIR));

        String fakeConfigJson = """
                        {
                           "intersectionParameters": {
                              "weightQueue": 1.0,
                               "weightWaitTime": 2.0
                           },
                          "intersectionTypes": {
                            "STANDARD": {
                              "roads": {
                              },
                              "phases": [
                              ]
                            },
                            "MULTI_LANES_STANDARD": {
                              "roads": {
                              },
                              "phases": [
                              ]
                            }
                          }
                        }
                """;
        Files.writeString(Paths.get(TEST_CONFIG_FILE), fakeConfigJson);

        IntersectionConfig.setConfigPath(TEST_CONFIG_FILE);
        IntersectionConfig.loadConfig();
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(Paths.get(TEST_CONFIG_FILE));
    }

    @Test
    void shouldCreateMultiLaneIntersection() {
        IntersectionSimulator simulator = new IntersectionSimulator();
        Command mockCommand = mock(Command.class);

        when(mockCommand.execute(any(Intersection.class))).thenReturn(List.of());

        simulator.runSimulation(List.of(mockCommand), "multi", "MULTI_LANES_STANDARD");

        verify(mockCommand).execute(intersectionCaptor.capture());
        Intersection capturedIntersection = intersectionCaptor.getValue();

        assertInstanceOf(MultiLaneIntersection.class, capturedIntersection);
    }

    @Test
    void shouldCreateSingleLaneIntersection() {
        IntersectionSimulator simulator = new IntersectionSimulator();
        Command mockCommand = mock(Command.class);
        when(mockCommand.execute(any(Intersection.class))).thenReturn(List.of());

        simulator.runSimulation(List.of(mockCommand), "single", "STANDARD");

        verify(mockCommand).execute(intersectionCaptor.capture());
        Intersection capturedIntersection = intersectionCaptor.getValue();

        assertInstanceOf(SingleLaneIntersection.class, capturedIntersection);
    }
}