package com.traffic_lights.config;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.traffic_lights.dto.intersection.IntersectionLayout;
import com.traffic_lights.dto.intersection.IntersectionParameters;
import com.traffic_lights.dto.intersection.RoadsConfiguration;
import lombok.extern.slf4j.Slf4j;
import java.io.File;


/**
 * Global configuration manager for traffic intersections.
 * This class handles loading, storing, and providing access to intersection layouts
 * and simulation parameters from a JSON configuration file.
 */
@Slf4j
public class IntersectionConfig {

    /** Default path to the intersection configuration file. */
    private static String path = "data/config/intersection_config.json";

    /** Cached roads configuration containing all intersection types. */
    private static RoadsConfiguration configuration;

    /** Configured ObjectMapper with case-insensitive enum support. */
    private static final ObjectMapper mapper = JsonMapper.builder()
                        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                        .build();


    /**
     * Updates the path to the configuration file.
     *
     * @param newPath The relative or absolute path to the JSON config file.
     */
    public static void setConfigPath(String newPath) {
        path = newPath;
    }

    /**
     * Loads the intersection configuration and phases from the defined file path into memory.
     * * @throws RuntimeException if the file cannot be found, read, or parsed correctly
     */
    public static void loadConfig() {
        try {
            configuration = mapper.readValue(new File(path), RoadsConfiguration.class);
            log.info("Successfully loaded road configurations and light phases for {} types.", configuration.intersectionTypes().size());
        } catch (Exception e) {
            log.error("Failed to load roads configuration!", e);
            throw new RuntimeException("Cannot start simulation without config");
        }
    }

    /**
     * Retrieves the structural layout for a specific intersection type.
     *
     * @param type The unique identifier of the intersection type
     * @return The {@link IntersectionLayout} containing roads and lanes data.
     * @throws IllegalArgumentException if the configuration has not been loaded or the type is unknown.
     */
    public static IntersectionLayout getLayoutForType(String type) {
        if (configuration == null || !configuration.intersectionTypes().containsKey(type)) {
            throw new IllegalArgumentException("Configuration not found for type: " + type);
        }
        return configuration.intersectionTypes().get(type);
    }

    /**
     * Provides access to parameters that configures optimization mechanism.
     *
     * @return The {@link IntersectionParameters} object containing global settings.
     * @throws IllegalArgumentException if the configuration has not been loaded.
     */
    public static IntersectionParameters getParameters() {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration parameters not found");
        }
        return configuration.intersectionParameters();
    }
}
