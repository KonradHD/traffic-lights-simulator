package com.traffic_lights.engine;

import java.util.ArrayList;
import java.util.List;

import com.traffic_lights.commands.Command;
import com.traffic_lights.commands.StepCommand;
import com.traffic_lights.components.Intersection;
import com.traffic_lights.dto.SimulationOutput;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IntersectionSimulator {

    public SimulationOutput runSimulation(List<Command> commands, String intersectionType) {
        log.info("Starting simulation for {} intersection", intersectionType);

        Intersection intersection = new Intersection(intersectionType);
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
