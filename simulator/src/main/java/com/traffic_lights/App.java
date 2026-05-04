package com.traffic_lights;

import com.traffic_lights.dto.SimulationInput;
import com.traffic_lights.dto.SimulationOutput;
import com.traffic_lights.engine.IntersectionSimulator;
import com.traffic_lights.parsing.InputParser;
import com.traffic_lights.parsing.OutputParser;


public class App 
{
    public static void main( String[] args )
    {
        SimulationInput input = InputParser.readFile("example1.json");
        IntersectionSimulator simulator = new IntersectionSimulator();

        SimulationOutput output = simulator.runSimulation(input.commands(), "standard");

        OutputParser.saveOutput("example1", output);

    }
}
