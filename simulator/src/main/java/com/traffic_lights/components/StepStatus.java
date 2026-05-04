package com.traffic_lights.components;

import java.util.ArrayList;
import java.util.List;

public class StepStatus {

    private final List<String> leftVehicles;
    
    public StepStatus(List<String> leftVehicles){
        int newSize = leftVehicles.size();
        this.leftVehicles = new ArrayList<>(newSize);

        for(int i = 0; i < newSize; i++){
            this.leftVehicles.add(leftVehicles.get(i));    
        }
    }

    public void addVehicle(String vehicleId){
        leftVehicles.add(vehicleId);
    }
}
