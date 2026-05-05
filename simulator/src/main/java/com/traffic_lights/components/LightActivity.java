package com.traffic_lights.components;

import com.traffic_lights.config.TimeConfig;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LightActivity {
    
    private LightState state;
    private int currentDuration;
    private final TimeConfig timeConfig;


    public void changeState(LightState newState){
        this.state = newState;
        this.currentDuration = this.timeConfig.getDurationFor(state);
    }

    public LightActivity(LightState state, TimeConfig timeConfig){
        this.state = state;
        this.timeConfig = timeConfig;
        this.currentDuration = this.timeConfig.getDurationFor(this.state);
    }


    public void nextPhase(){
        LightState nextState = this.state.nextState();
        changeState(nextState);
    }


    public void nextStep(){
        if(this.currentDuration > 1){
            this.currentDuration--;
        }else{
            nextPhase();
        }
    }

    public void addTime(int addingTime){
        this.currentDuration += addingTime;
    }
    
    public LightState getState() {
        return this.state;
    }
}
