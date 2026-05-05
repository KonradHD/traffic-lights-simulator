package com.traffic_lights.dto.intersection;

import com.traffic_lights.components.Turn;

import java.util.List;

public record LaneDTO(
        List<Turn> allowedTurns
) {
}
