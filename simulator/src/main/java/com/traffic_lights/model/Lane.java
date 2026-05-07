package com.traffic_lights.model;

import com.traffic_lights.model.LightType;
import com.traffic_lights.model.TrafficLight;
import com.traffic_lights.dto.Vehicle;
import com.traffic_lights.dto.intersection.LaneDTO;
import lombok.Getter;
import com.traffic_lights.model.LightState;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.List;
import java.util.Queue;

@Getter
public class Lane {

    private final List<Turn> availableTurns;
    private final Queue<Vehicle> vehicles = new ArrayDeque<>();
    private final List<TrafficLight> trafficLights;


    public Lane(List<Turn> allowedTurns, List<TrafficLight> lights) {
        this.availableTurns = allowedTurns;
        this.trafficLights = lights;
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    public boolean canHandleTurn(Turn turn) {
        return availableTurns.contains(turn);
    }

    public List<TrafficLight> getLightsForTurn(Turn intendedTurn) {

        return trafficLights.stream()
                .filter(light -> light.getTurnGroup().includes(intendedTurn))
                .toList();
    }

    public boolean isLightGreen(Turn intendedTurn) {
        List<TrafficLight> applicableLights = getLightsForTurn(intendedTurn);

        return applicableLights.stream()
                .anyMatch(TrafficLight::isGreen);
    }

    public static Lane createLaneFromDTO(LaneDTO dto){
        return new Lane(
                dto.allowedTurns(),
                dto.trafficLights().stream().map(TrafficLight::createTrafficLightFromDTO).toList()
        );
    }


    public void applyCurrentPhase(List<Turn> greenTurns) {
        if (greenTurns == null || greenTurns.isEmpty()) {
            for (TrafficLight light : trafficLights) {
                light.setLightState(LightState.RED);
            }
            return;
        }

        for (TrafficLight light : trafficLights) {
            boolean shouldBeGreen = light.getTurnGroup().getTurns().stream()
                    .anyMatch(greenTurns::contains);

            if (shouldBeGreen) {
                light.setLightState(LightState.GREEN);
            } else {
                light.setLightState(LightState.RED);
            }
        }
    }
}
