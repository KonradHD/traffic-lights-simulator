package com.traffic_lights.utils;

import java.io.File;
import java.time.Instant;

import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.traffic_lights.dto.SimulationOutput;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class responsible for exporting simulation results to external files.
 * <p>
 * This class uses the Jackson {@link ObjectMapper} to serialize {@link SimulationOutput}
 * objects into a formatted JSON structure.
 * </p>
 */
@Slf4j
public class OutputParser {

    /**
     * Jackson object mapper configured for JSON serialization.
     */
    private final static ObjectMapper mapper = new ObjectMapper();

    /**
     * Serializes the simulation results and saves them to a specified JSON file.
     * <p>
     * The method enables {@link SerializationFeature#INDENT_OUTPUT} to ensure the
     * resulting JSON is human-readable. It also performs a check for the existence
     * of the parent directory and creates it if necessary.
     * </p>
     *
     * @param fileName The path and name of the file to be created.
     * @param output   The {@link SimulationOutput} data containing the simulation state history.
     */
    public static void saveOutput(String fileName, SimulationOutput output){
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

//        Long epochs = Instant.now().getEpochSecond();
//        String fullPath = "%s%d_%s".formatted(outputPath, epochs, fileName);

        File fileOutput = new File(fileName);

        try{
            File parentDir = fileOutput.getParentFile();

            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            mapper.writeValue(fileOutput, output);
            log.info("Successfully saved simulation output to: {}", fileName);

        }catch(IOException e ){
            log.error("Cannot save output file: {}, {}", fileName, e.getMessage());
        }
    }
}
