package com.traffic_lights.engine;

import java.util.ArrayList;
import java.util.List;

import com.traffic_lights.commands.Command;
import com.traffic_lights.commands.StepCommand;
import com.traffic_lights.components.Intersection;
import com.traffic_lights.components.StepStatus;

public class IntersectionSimulator {

    public List<StepStatus> runSimulation(List<Command> commands, String intersectionType) {
        Intersection intersection = new Intersection(intersectionType);
        List<StepStatus> simulationResult = new ArrayList<>();

        for (Command command : commands) {
            List<String> leftVehicles = command.execute(intersection);
            
            if (command instanceof StepCommand) {
                simulationResult.add(new StepStatus(leftVehicles));
            }
        }

        return simulationResult;
    }
    
}
