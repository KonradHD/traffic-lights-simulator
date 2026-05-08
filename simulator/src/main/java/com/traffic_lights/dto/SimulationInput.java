package com.traffic_lights.dto;

import com.traffic_lights.commands.Command;
import java.util.List;

/**
 * Data Transfer Object representing the complete set of instructions for a simulation run.
 * <p>
 * This record acts as the primary container for data parsed from the input JSON file.
 * It holds an ordered sequence of polymorphic {@link Command} objects that define
 * the actions to be performed on the intersection.
 * </p>
 */
public record SimulationInput(
    List<Command> commands
){

}