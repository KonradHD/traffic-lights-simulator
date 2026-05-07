package com.traffic_lights.model;

import com.traffic_lights.model.Turn;
import com.traffic_lights.dto.intersection.TrafficLightDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TrafficLight {

    @Setter
    private LightState lightState;

    private final TurnGroup turnGroup;
    private final LightType lightType;

    public void nextLight(){
        lightState.nextState();
    }

    public List<Turn> greenLightTurns() {
        if (lightState == null || lightState != LightState.GREEN) {
            return List.of();
        }

        return turnGroup.getTurns();
    }

    public static TrafficLight createTrafficLightFromDTO(TrafficLightDTO dto) {
        return new TrafficLight(
                dto.lightState(),
                dto.turnGroup(),
                dto.lightType()
        );
    }

    public boolean isGreen() {
        return lightState == LightState.GREEN;
    }
}
