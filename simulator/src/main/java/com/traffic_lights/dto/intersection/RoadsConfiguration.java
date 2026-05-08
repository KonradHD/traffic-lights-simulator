package com.traffic_lights.dto.intersection;

import java.util.Map;

/**
 * Data Transfer Object representing the root configuration for the traffic simulation system.
 * <p>
 * This record acts as the top-level container for all intersection-related settings
 * parsed from the global configuration file.
 * </p>
 */
public record RoadsConfiguration(
        IntersectionParameters intersectionParameters,
        Map<String, IntersectionLayout> intersectionTypes
) {
}
