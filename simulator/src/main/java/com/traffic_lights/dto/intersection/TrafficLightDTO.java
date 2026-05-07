package com.traffic_lights.dto.intersection;

import com.traffic_lights.model.Turn;
import com.traffic_lights.model.LightState;
import com.traffic_lights.model.LightType;
import com.traffic_lights.model.TurnGroup;

public record TrafficLightDTO(
        LightState lightState,
        TurnGroup turnGroup,
        LightType lightType
) {

}
