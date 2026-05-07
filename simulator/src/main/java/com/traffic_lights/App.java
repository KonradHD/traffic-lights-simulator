package com.traffic_lights;

import java.io.IOException;

import com.traffic_lights.dto.SimulationInput;
import com.traffic_lights.dto.SimulationOutput;
import com.traffic_lights.engine.IntersectionSimulator;
import com.traffic_lights.utils.InputParser;
import com.traffic_lights.utils.OutputParser;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class App 
{
    public static void main(String[] args)
    {
        if (args.length < 2) {
            log.error("Invalid number of arguments!");
            log.error("Usage: java -jar simulator.jar <input.json> <output.json> [intersection_type]");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        String intersectionType = (args.length >= 3) ? args[2] : "MULTI_LANES_STANDARD";
        String intersectionStyle = intersectionType.split("_")[0];

        log.info("Starting simulation...");
        log.info("Input file: {}, output file: {}, Intersection type: {}", inputFile, outputFile, intersectionType);

        try {
            SimulationInput input = InputParser.readFile(inputFile);
            IntersectionSimulator simulator = new IntersectionSimulator();

            SimulationOutput output = simulator.runSimulation(input.commands(), intersectionStyle.toUpperCase(), intersectionType.toUpperCase());

            OutputParser.saveOutput(outputFile, output);

        } catch (IOException e) {
            log.error("File Input/Output error: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected simulation error: {}", e.getMessage(), e);
        }
    }
}
