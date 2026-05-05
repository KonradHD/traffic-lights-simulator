package com.traffic_lights.dto;


import com.traffic_lights.components.Direction;

public record Vehicle(

    String id,
    Direction startRoad,
    Direction endRoad

)  {
}
