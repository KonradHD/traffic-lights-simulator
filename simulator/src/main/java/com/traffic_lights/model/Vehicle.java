package com.traffic_lights.model;


public record Vehicle(

    String id,
    Direction startRoad,
    Direction endRoad

)  {
}
