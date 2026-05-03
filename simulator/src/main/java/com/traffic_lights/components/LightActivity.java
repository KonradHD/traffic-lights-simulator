package com.traffic_lights.components;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LightActivity {
    
    private LightState state;
    private Integer timeToChange;


    public void changeState(LightState newState, Integer duration){
        this.state = newState;
        this.timeToChange = duration;
    }


    public void changeState(LightState newState){
        this.state = newState;
        this.timeToChange = 30;
    }

    public void nextStep(){
        if(timetoChange > 1){
            timeToChange--;
        }
        else if(timeToChange == 1){
            
        }
    }
}
