package com.traffic_lights.dto.intersection;

import com.traffic_lights.components.Turn;
import com.traffic_lights.components.lights.LightState;
import com.traffic_lights.components.lights.LightType;

public record TrafficLightDTO(
        LightState lightState,
        Turn turn,
        LightType lightType
) {

}
