package com.traffic_lights.dto.intersection;

import com.traffic_lights.model.Turn;

import java.util.List;

public record LaneDTO(
        List<Turn> allowedTurns,
        List<TrafficLightDTO> trafficLights
) {
}
