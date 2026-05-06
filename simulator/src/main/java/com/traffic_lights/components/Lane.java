package com.traffic_lights.components;

import com.traffic_lights.components.lights.LightType;
import com.traffic_lights.components.lights.TrafficLight;
import com.traffic_lights.dto.Vehicle;
import com.traffic_lights.dto.intersection.LaneDTO;
import lombok.Getter;
import com.traffic_lights.components.lights.LightState;

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

    public Optional<TrafficLight> getLightForTurn(Turn intendedTurn) {
        for (TrafficLight light : trafficLights) {
            if (light.getTurn() == intendedTurn) {
                return Optional.of(light);
            }
        }

        for (TrafficLight light : trafficLights) {
            if (light.getType() == LightType.GENERAL) {
                return Optional.of(light);
            }
        }

        return Optional.empty();
    }

    public boolean isLightGreen(Turn intendedTurn) {
        for (TrafficLight light : trafficLights) {
            if (light.getTurn() == intendedTurn && light.getLightState() == LightState.GREEN) {
                return true;
            }
        }

        for (TrafficLight light : trafficLights) {
            if (light.getType() == LightType.GENERAL && light.getLightState() == LightState.GREEN) {
                return availableTurns.contains(intendedTurn);
            }
        }

        return false;
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

            if (greenTurns.contains(light.getTurn())) {
                light.setLightState(LightState.GREEN);
            } else {
                light.setLightState(LightState.RED);
            }
        }
    }
}
