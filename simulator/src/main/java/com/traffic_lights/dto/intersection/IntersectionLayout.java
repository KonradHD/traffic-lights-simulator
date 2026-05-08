package com.traffic_lights.dto.intersection;

import com.traffic_lights.model.Direction;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object representing the static structural template of an intersection.
 * <p>
 * This record maps out the physical and logical architecture of a specific intersection type
 * as defined in the application configuration. It combines the physical infrastructure with the signaling logic.
 * </p>
 */
public record IntersectionLayout(
        Map<Direction, List<LaneDTO>> roads,
        List<IntersectionPhaseDTO> phases
) {
}
