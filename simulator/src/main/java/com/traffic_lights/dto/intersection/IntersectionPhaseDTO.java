package com.traffic_lights.dto.intersection;

import com.traffic_lights.model.Direction;
import com.traffic_lights.model.Turn;

import java.util.List;
import java.util.Map;

public record IntersectionPhaseDTO(
    int maxDuration,
    Map<Direction, List<Turn>> paths
) {
}
