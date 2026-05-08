package com.traffic_lights;

import java.io.IOException;

import com.traffic_lights.dto.SimulationInput;
import com.traffic_lights.dto.SimulationOutput;
import com.traffic_lights.engine.IntersectionSimulator;
import com.traffic_lights.utils.InputParser;
import com.traffic_lights.utils.OutputParser;

import lombok.extern.slf4j.Slf4j;

/**
 * The main entry point for the Traffic Light Simulator.
 * <p>
 * This class handles command-line arguments, initializes the simulation environment,
 * and orchestrates the process of reading input, executing the simulation logic,
 * and saving the results.
 * </p>
 */
@Slf4j
public class App 
{

    /**
     * Main method that serves as the application lifecycle controller.
     * <p>
     * Logic flow:
     * <ol>
     * <li>Validates command-line arguments.</li>
     * <li>Determines {@code intersectionType} and derives {@code intersectionStyle}</li>
     * <li>Parses the input JSON file into a sequence of commands.</li>
     * <li>Invokes the {@link IntersectionSimulator} to process the logic.</li>
     * <li>Persists the resulting {@link SimulationOutput} to the specified file.</li>
     * </ol>
     * </p>
     *
     * @param args Command-line arguments:
     * [0] - Path to input JSON file (Required).
     * [1] - Path to output JSON file (Required).
     * [2] - Intersection type string (Optional, defaults to "MULTI_LANES_STANDARD").
     */
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
