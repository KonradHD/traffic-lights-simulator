package com.traffic_lights.commands;

import java.util.List;

import com.traffic_lights.intersection.Intersection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record StepCommand() implements Command{
    
    @Override
    public List<String> execute(Intersection intersection) {
        log.info("Executing step command...");
        return intersection.processStep();
    }
    
}
