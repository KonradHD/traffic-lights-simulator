package com.traffic_lights.config;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.traffic_lights.dto.intersection.IntersectionLayout;
import com.traffic_lights.dto.intersection.RoadsConfiguration;
import lombok.extern.slf4j.Slf4j;
import java.io.File;

@Slf4j
public class IntersectionConfig {
    private static final String path = "data/config/intersection_config.json";
    private static RoadsConfiguration configuration;
    private static final ObjectMapper mapper = JsonMapper.builder()
                        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                        .build();

    public static void loadConfig() {
        try {
            configuration = mapper.readValue(new File(path), RoadsConfiguration.class);
            log.info("Successfully loaded road configurations and light phases for {} types.", configuration.configs().size());
        } catch (Exception e) {
            log.error("Failed to load roads configuration!", e);
            throw new RuntimeException("Cannot start simulation without config");
        }
    }

    public static IntersectionLayout getLayoutForType(String type) {
        if (configuration == null || !configuration.configs().containsKey(type)) {
            throw new IllegalArgumentException("Configuration not found for type: " + type);
        }
        return configuration.configs().get(type);
    }
}
