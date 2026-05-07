package com.traffic_lights.dto;


import com.traffic_lights.model.Direction;

public record Vehicle(

    String id,
    Direction startRoad,
    Direction endRoad

)  {
}
