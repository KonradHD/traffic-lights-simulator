package com.traffic_lights.dto;

import com.traffic_lights.commands.Command;
import java.util.List;


public record SimulationInput(
    List<Command> commands
){

}