package com.traffic_lights.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.traffic_lights.dto.SimulationInput;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;


@Slf4j
public class InputParser {

//    private static final String inputPath = "";
    private static final ObjectMapper mapper = new ObjectMapper();


    public static SimulationInput readFile(String fileName) throws IOException{
//        String fullPath = inputPath + fileName;
        File inputFile = new File(fileName);
        SimulationInput input;


        input = mapper.readValue(inputFile, SimulationInput.class);
        log.info("Uploaded {} commands", input.commands().size());
        
        return input;
    }
    
}
