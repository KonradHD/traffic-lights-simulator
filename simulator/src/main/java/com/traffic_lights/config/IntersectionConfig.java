package com.traffic_lights.config;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.traffic_lights.dto.intersection.IntersectionLayout;
import com.traffic_lights.dto.intersection.IntersectionParameters;
import com.traffic_lights.dto.intersection.RoadsConfiguration;
import lombok.extern.slf4j.Slf4j;
import java.io.File;

@Slf4j
public class IntersectionConfig {
    private static String path = "data/config/intersection_config.json";
    private static RoadsConfiguration configuration;
    private static final ObjectMapper mapper = JsonMapper.builder()
                        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                        .build();


    public static void setConfigPath(String newPath) {
        path = newPath;
    }

    public static void loadConfig() {
        try {
            configuration = mapper.readValue(new File(path), RoadsConfiguration.class);
            log.info("Successfully loaded road configurations and light phases for {} types.", configuration.intersectionTypes().size());
        } catch (Exception e) {
            log.error("Failed to load roads configuration!", e);
            throw new RuntimeException("Cannot start simulation without config");
        }
    }

    public static IntersectionLayout getLayoutForType(String type) {
        if (configuration == null || !configuration.intersectionTypes().containsKey(type)) {
            throw new IllegalArgumentException("Configuration not found for type: " + type);
        }
        return configuration.intersectionTypes().get(type);
    }

    public static IntersectionParameters getParameters() {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration parameters not found");
        }
        return configuration.intersectionParameters();
    }
}
