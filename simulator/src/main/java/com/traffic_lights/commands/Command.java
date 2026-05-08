package com.traffic_lights.commands;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.traffic_lights.intersection.Intersection;

/**
 * Represents an executable action within the traffic simulation.
 * <p>
 * This interface acts as the base contract for the Command Design Pattern,
 * allowing the simulation to process a sequence of varied instructions.
 * </p>
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = AddVehicleCommand.class, name = "addVehicle"),
    @JsonSubTypes.Type(value = StepCommand.class, name = "step")
})
public interface Command {

    /**
     * Executes the specific logic of the command against the provided intersection context.
     * * @param intersection The central intersection state manager that acts as the
     * receiver of this command.
     * @return A {@link List} of strings representing the unique identifiers of vehicles
     * that successfully left the intersection.
     */
    List<String> execute(Intersection intersection);

}
