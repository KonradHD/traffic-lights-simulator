package com.traffic_lights.parsing;

import java.io.File;
import java.time.Instant;

import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traffic_lights.dto.SimulationOutput;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OutputParser {

    private final static String outputPath = "data/output/";
    private final static ObjectMapper mapper = new ObjectMapper();

    public static void saveOutput(String fileName, SimulationOutput output){
        Long epochs = Instant.now().getEpochSecond();
        String fullPath = "%s%d_%s".formatted(outputPath, epochs, fileName);

        File fileOutput = new File(fullPath);

        try{
            
            fileOutput.getParentFile().mkdirs();
            mapper.writeValue(fileOutput, output);
            log.info("Successfully saved simulation output to: {}", fullPath);

        }catch(IOException e ){
            log.error("Cannot save output file: {}, {}", fullPath, e.getMessage());
        }
    }
}
