package com.traffic_lights.dto.intersection;

import com.traffic_lights.model.LightState;
import com.traffic_lights.model.LightType;
import com.traffic_lights.model.TurnGroup;

/**
 * Data Transfer Object representing the initial state and hardware configuration
 * of a traffic signal.
 * <p>
 * This record is used during the intersection initialization process to define
 * how a specific traffic light should behave, what movements it controls,
 * and its starting state.
 * </p>
 */
public record TrafficLightDTO(
        LightState lightState,
        TurnGroup turnGroup,
        LightType lightType
) {

}
