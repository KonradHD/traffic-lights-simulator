package com.traffic_lights.dto.intersection;

import com.traffic_lights.components.Direction;
import com.traffic_lights.components.Turn;

import java.util.List;
import java.util.Map;

public record IntersectionPhaseDTO(
    int maxDuration,
    Map<Direction, List<Turn>> paths
) {
}
