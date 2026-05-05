package com.traffic_lights;

import java.io.IOException;

import com.traffic_lights.dto.SimulationInput;
import com.traffic_lights.dto.SimulationOutput;
import com.traffic_lights.engine.IntersectionSimulator;
import com.traffic_lights.parsing.InputParser;
import com.traffic_lights.parsing.OutputParser;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class App 
{
    public static void main( String[] args )
    {
        String fileName = "example3.json";
        try {

            SimulationInput input = InputParser.readFile(fileName);
            IntersectionSimulator simulator = new IntersectionSimulator();
    
            SimulationOutput output = simulator.runSimulation(input.commands(), "MULTI_LANES_STANDARD");
    
            OutputParser.saveOutput(fileName, output);
            
        } catch (IOException e) {
            log.error("Cannot open file: {}, {}", fileName, e.getMessage());
        }

    }
}
