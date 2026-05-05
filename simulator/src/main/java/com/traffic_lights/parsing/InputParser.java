package com.traffic_lights.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.traffic_lights.dto.SimulationInput;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class InputParser {

    private static final String INPUTPATH = "data/input/";
    private static final ObjectMapper mapper = new ObjectMapper();


    public static SimulationInput readFile(String fileName) throws IOException{
        String fullPath = INPUTPATH + fileName;
        File inputFile = new File(fullPath);
        SimulationInput input;


        input = mapper.readValue(inputFile, SimulationInput.class);
        log.info("Uploaded {} commands", input.commands().size());
        
        return input;
    }
    
}
