package com.traffic_lights.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.traffic_lights.dto.SimulationInput;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

/**
 * Utility class responsible for parsing simulation input data from external files.
 * <p>
 * This class utilizes the Jackson {@link ObjectMapper} to deserialize JSON files
 * into {@link SimulationInput} objects, which contain the sequence of commands
 * to be executed during the simulation.
 * </p>
 */
@Slf4j
public class InputParser {

//    private static final String inputPath = "";
    /** * Jackson object mapper used for JSON deserialization.
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Reads a JSON file from the specified path and converts it into a simulation input model.
     * <p>
     * The method expects a valid JSON structure that maps directly to the {@link SimulationInput}
     * record..
     * </p>
     *
     * @param fileName The path to the input JSON file (relative or absolute).
     * @return A {@link SimulationInput} object populated with the data from the file.
     * @throws IOException if the file does not exist, cannot be read, or contains invalid JSON
     * that does not match the expected data model.
     */
    public static SimulationInput readFile(String fileName) throws IOException{
//        String fullPath = inputPath + fileName;
        File inputFile = new File(fileName);
        SimulationInput input;

        input = mapper.readValue(inputFile, SimulationInput.class);
        log.info("Uploaded {} commands", input.commands().size());
        
        return input;
    }
    
}
