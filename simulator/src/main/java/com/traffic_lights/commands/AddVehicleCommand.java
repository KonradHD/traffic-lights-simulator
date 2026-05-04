package com.traffic_lights.commands;

import java.util.Collections;
import java.util.List;

import com.traffic_lights.components.Direction;
import com.traffic_lights.components.Intersection;
import com.traffic_lights.components.Vehicle;

public record AddVehicleCommand(

    String vehicleId, 
    Direction startRoad, 
    Direction endRoad
    
) implements Command {

    @Override
    public List<String> execute(Intersection intersection) {
        // Tworzymy obiekt pojazdu na podstawie danych z komendy
        Vehicle vehicle = new Vehicle(vehicleId, startRoad, endRoad);
        
        // Zlecamy skrzyżowaniu umieszczenie auta na odpowiedniej drodze
        intersection.addVehicleToQueue(vehicle);
        
        // Ta komenda nie wypuszcza aut ze skrzyżowania, więc zwraca pustą listę
        return Collections.emptyList();
    }
}
