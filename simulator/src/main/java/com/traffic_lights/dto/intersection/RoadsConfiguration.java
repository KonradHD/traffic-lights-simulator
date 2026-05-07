package com.traffic_lights.dto.intersection;

import java.util.Map;

public record RoadsConfiguration(
        IntersectionParameters intersectionParameters,
        Map<String, IntersectionLayout> intersectionTypes
) {
}
