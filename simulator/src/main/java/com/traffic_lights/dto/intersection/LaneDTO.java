package com.traffic_lights.dto.intersection;

import com.traffic_lights.model.Turn;

import java.util.List;

/**
 * Data Transfer Object representing the configuration and infrastructure of a single traffic lane.
 * <p>
 * This record captures the physical properties of a lane as defined in the configuration files,
 * specifically outlining what maneuvers a vehicle can perform and which signal hardware
 * is present to control those movements.
 * </p>
 */
public record LaneDTO(
        List<Turn> allowedTurns,
        List<TrafficLightDTO> trafficLights
) {
}
