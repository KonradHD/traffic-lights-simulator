package com.traffic_lights.components;

import com.traffic_lights.dto.Vehicle;
import lombok.Getter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

@Getter
public class Lane {

    private final List<Turn> availableTurns;
    private final Queue<Vehicle> vehicles = new ArrayDeque<>();


    public Lane(List<Turn> allowedTurns) {
        this.availableTurns = allowedTurns;
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    public boolean canHandleTurn(Turn turn) {
        return availableTurns.contains(turn);
    }
}
