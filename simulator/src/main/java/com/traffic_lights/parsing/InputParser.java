package com.traffic_lights.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

import com.traffic_lights.dto.SimulationInput;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class InputParser {

    private static final String inputPath = "data/input/";
    private static final ObjectMapper mapper = new ObjectMapper();


    public static SimulationInput readFile(String fileName){
        String fullPath = inputPath + fileName;
        File inputFile = new File(fullPath);

        try {

            SimulationInput input = mapper.readValue(inputFile, SimulationInput.class);
            log.info("Uploaded {} commands", input.commands().size());
            return input;

        } catch (IOException e) {
            log.error("Cannot open file: {}, {}", fullPath, e.getMessage());
        }
    }
    
}
