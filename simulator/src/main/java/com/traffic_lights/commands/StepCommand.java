package com.traffic_lights.commands;

import java.util.List;

import com.traffic_lights.components.Intersection;

public record StepCommand() implements Command{
    
    @Override
    public List<String> execute(Intersection intersection) {
        return intersection.processStep(); 
    }
    
}
