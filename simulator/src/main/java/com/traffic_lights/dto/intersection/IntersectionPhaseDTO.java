package com.traffic_lights.dto.intersection;

import com.traffic_lights.model.Direction;
import com.traffic_lights.model.Turn;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object representing a single signal phase configuration within an intersection layout.
 * <p>
 * This record defines which maneuvers are legally permitted from specific road approaches
 * during a distinct period of the traffic light cycle.
 * </p>
 */
public record IntersectionPhaseDTO(
    int basicDuration,
    Map<Direction, List<Turn>> paths
) {
}
