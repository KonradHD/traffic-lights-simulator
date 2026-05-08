package com.traffic_lights.commands;

import java.util.List;

import com.traffic_lights.intersection.Intersection;
import lombok.extern.slf4j.Slf4j;

/**
 * A command representing a single discrete time step in the traffic simulation.
 * <p>
 * When executed, this command triggers the internal logic of the intersection
 * to process vehicle movements, update waiting times, and manage light phase transitions
 * for the current time increment.
 * </p>
 */
@Slf4j
public record StepCommand() implements Command{

    /**
     * Executes a single simulation step on the provided intersection.
     * <p>
     * This method delegates the logic to the {@link Intersection#processStep()} method,
     * which returns a list of vehicle identifiers that successfully cleared the
     * intersection during this specific step.
     * </p>
     */
    @Override
    public List<String> execute(Intersection intersection) {
        log.info("Executing step command...");
        return intersection.processStep();
    }
    
}
