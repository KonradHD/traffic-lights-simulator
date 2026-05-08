package com.traffic_lights.engine;

import java.util.List;

import com.traffic_lights.commands.Command;
import com.traffic_lights.commands.StepCommand;
import com.traffic_lights.intersection.Intersection;
import com.traffic_lights.intersection.IntersectionFactory;
import com.traffic_lights.dto.SimulationOutput;
import lombok.extern.slf4j.Slf4j;

/**
 * The core execution engine for the traffic light simulation.
 * <p>
 * This class is responsible for orchestrating the overall simulation lifecycle.
 * It coordinates the creation of the intersection environment and processes a
 * sequential list of commands to simulate traffic flow over time, aggregating
 * the results into a structured output.
 * </p>
 */
@Slf4j
public class IntersectionSimulator {

    /**
     * Executes the traffic simulation based on the provided configuration and command sequence.
     * <p>
     * The method utilizes the {@link IntersectionFactory} to construct the appropriate
     * intersection instance. It then iterates through the provided commands. If a command represents a simulation step
     * (i.e., {@link StepCommand}), the vehicles that successfully leave the intersection
     * during that step are recorded in the final output.
     * </p>
     *
     * @param commands          The ordered list of instructions to execute.
     * @param intersectionStyle The general structural style of the intersection (e.g. "single", "multi").
     * @param intersectionType  The specific configuration identifier mapping to a defined road layout and phase schedule.
     * @return A {@link SimulationOutput} object containing the historical record of departed vehicles per step.
     */
    public SimulationOutput runSimulation(List<Command> commands, String intersectionStyle, String intersectionType) {
        log.info("Starting simulation for {} intersection", intersectionType);

        Intersection intersection = IntersectionFactory.createIntersection(intersectionStyle, intersectionType);

        SimulationOutput output = SimulationOutput.createEmptySimOutput();
        for (Command command : commands) {
            List<String> leftVehicles = command.execute(intersection);

            if (command instanceof StepCommand) {
                output.addVehicles(leftVehicles);
            }
            log.info(intersection.getStats().toString());
        }

        return output;
    }
    
}
