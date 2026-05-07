package com.traffic_lights.dto.intersection;

public record IntersectionParameters(
        double weightQueue,
        double weightWaitTime
) {
}
