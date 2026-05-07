package com.traffic_lights.config;

import com.traffic_lights.dto.intersection.IntersectionLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class IntersectionConfigTest {

    private static final String TEST_DIR = "data/test_config/";
    private static final String VALID_FILE = TEST_DIR + "valid_config.json";
    private static final String INVALID_FILE = TEST_DIR + "invalid_config.json";

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(Paths.get(TEST_DIR));

        String validJson = """
                {
                  "intersectionTypes": {
                    "STANDARD": {
                      "roads": {},
                      "phases": []
                    }
                  }
                }
                """;
        Files.writeString(Paths.get(VALID_FILE), validJson);
        String invalidJson = """
                {
                  "intersectionTypes": {
                    "STANDARD": 
                """;
        Files.writeString(Paths.get(INVALID_FILE), invalidJson);
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(Paths.get(VALID_FILE));
        Files.deleteIfExists(Paths.get(INVALID_FILE));

        Field configField = IntersectionConfig.class.getDeclaredField("configuration");
        configField.setAccessible(true);
        configField.set(null, null);
    }

    @Test
    void shouldLoadConfigSuccessfully() {
        IntersectionConfig.setConfigPath(VALID_FILE);

        IntersectionConfig.loadConfig();

        IntersectionLayout layout = IntersectionConfig.getLayoutForType("STANDARD");
        assertNotNull(layout);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenJsonIsMalformed() {
        IntersectionConfig.setConfigPath(INVALID_FILE);
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                IntersectionConfig::loadConfig
        );

        assertTrue(exception.getMessage().contains("Cannot start simulation without config"));
    }

    @Test
    void shouldReturnLayoutForExistingType() {
        IntersectionConfig.setConfigPath(VALID_FILE);
        IntersectionConfig.loadConfig();
        IntersectionLayout layout = IntersectionConfig.getLayoutForType("STANDARD");

        assertNotNull(layout, "Layout powinien zostać poprawnie zwrócony");
    }


    @Test
    void shouldThrowIllegalArgumentExceptionWhenConfigIsNotLoaded() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> IntersectionConfig.getLayoutForType("STANDARD")
        );

        assertEquals("Configuration not found for type: STANDARD", exception.getMessage());
    }
}
