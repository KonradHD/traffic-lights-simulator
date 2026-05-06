package com.traffic_lights.dto.intersection;

import com.traffic_lights.components.Direction;

import java.util.List;
import java.util.Map;

public record IntersectionLayout(
        Map<Direction, List<LaneDTO>> roads,
        List<IntersectionPhaseDTO> phases,
        Map<Direction, List<TrafficLightDTO>> roadLights
) {
}
