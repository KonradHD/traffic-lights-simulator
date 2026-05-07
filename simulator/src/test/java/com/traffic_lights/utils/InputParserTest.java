package com.traffic_lights.utils;

import com.traffic_lights.dto.SimulationInput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class InputParserTest {

    private static final String TEST_DIR = "data/input/";
    private static final String VALID_FILE_NAME = "test_valid_input.json";
    private static final String INVALID_FILE_NAME = "test_invalid_input.json";

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(Paths.get(TEST_DIR));

        String validJson = """
        {
          "commands": [
            {
              "type": "addVehicle",
              "vehicleId": "vehicle1",
              "startRoad": "south",
              "endRoad": "north"
            },
            {
              "type": "step"
            }
          ]
        }
        """;
        Files.writeString(Paths.get(TEST_DIR + VALID_FILE_NAME), validJson);

        String invalidJson = """
            {
              "commands": [
                {
                  "type": "addVehicle"
            """;
        Files.writeString(Paths.get(TEST_DIR + INVALID_FILE_NAME), invalidJson);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_DIR + VALID_FILE_NAME));
        Files.deleteIfExists(Paths.get(TEST_DIR + INVALID_FILE_NAME));
    }

    @Test
    void shouldParseValidInputFileAndReturnSimulationInput() throws IOException {
        SimulationInput result = InputParser.readFile(TEST_DIR + VALID_FILE_NAME);

        assertNotNull(result);
        assertEquals(2, result.commands().size());
    }

    @Test
    void shouldThrowIOExceptionWhenJsonIsMalformed() {
        assertThrows(
                IOException.class,
                () -> InputParser.readFile(TEST_DIR + INVALID_FILE_NAME)
        );
    }
}
