package com.traffic_lights.dto.intersection;

import java.util.Map;

public record RoadsConfiguration(
        Map<String, IntersectionLayout> configs
) {
}
