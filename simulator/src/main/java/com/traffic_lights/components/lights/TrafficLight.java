package com.traffic_lights.components.lights;

import com.traffic_lights.components.Turn;
import com.traffic_lights.dto.intersection.TrafficLightDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class TrafficLight {

    @Setter
    private LightState lightState;

    private final Turn turn;
    private final LightType lightType;

    public void nextLight(){
        lightState.nextState();
    }

    public List<Turn> greenLightTurns(){
        if(lightState == null || lightState != LightState.GREEN){
            return List.of();
        }

        if(lightType == LightType.GENERAL){
            return List.of(Turn.STRAIGHT, Turn.LEFT, Turn.RIGHT);
        }

        return List.of(turn);
    }

    public static TrafficLight createTrafficLightFromDTO(TrafficLightDTO dto){
        return new TrafficLight(
                dto.lightState(),
                dto.turn(),
                dto.lightType()
        );
    }
}
