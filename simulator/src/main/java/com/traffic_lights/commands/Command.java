package com.traffic_lights.commands;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.traffic_lights.intersection.Intersection;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, 
    include = JsonTypeInfo.As.PROPERTY, 
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = AddVehicleCommand.class, name = "addVehicle"),
    @JsonSubTypes.Type(value = StepCommand.class, name = "step")
})
public interface Command {
    
    public List<String> execute(Intersection intersection);

}
