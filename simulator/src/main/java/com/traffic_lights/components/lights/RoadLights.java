package com.traffic_lights.components.lights;

import com.traffic_lights.components.Turn;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class RoadLights {

    List<TrafficLight> lights;


    public void nextStepForAll(){
        for(TrafficLight light : lights){
            light.nextLight();
        }
    }

    public void applyAllowedTurns(List<Turn> allowedTurns) {
        if (allowedTurns == null) {
            allowedTurns = new ArrayList<>();
        }

        List<Turn> finalAllowedTurns = allowedTurns;
        lights.stream()
                .filter(light -> finalAllowedTurns.contains(light.getTurn()))
                .forEach(TrafficLight::nextLight);
    }

    public List<Turn> getActiveTurns(){
        List<Turn> activeTurns = new ArrayList<>(); 

        for (TrafficLight light : lights) {
            activeTurns.addAll(light.greenLightTurns());
        }

        return activeTurns;
    }
}
