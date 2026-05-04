package com.traffic_lights.components;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;

@Getter
// @AllArgsConstructor
public class RoadLights {
    // light for driving straight or right
    private LightActivity mainLight; 
    
    // driving left
    private LightActivity leftTurnArrow; 
    
    // conditional driving right
    private LightActivity rightTurnArrow; 


    public RoadLights(LightActivity mainLight, LightActivity leftTurnArrow, LightActivity rightTurnArrow){
        this.mainLight = mainLight;
        this.leftTurnArrow = leftTurnArrow;
        this.rightTurnArrow = rightTurnArrow;
    }

    public LightActivity getMainLight(){
        return mainLight;
    }

    public LightActivity getLeftTurnArrow(){
        return leftTurnArrow;
    }

    public LightActivity getRightTurnArrow(){
        return rightTurnArrow;
    }

    public void nextStepForAll(){
        if(mainLight != null){
            mainLight.nextStep();
        }

        if(leftTurnArrow != null){
            mainLight.nextStep();
        }

        if(rightTurnArrow != null){
            rightTurnArrow.nextStep();
        }
    }

    public List<Turn> getActiveTurns(){
        List<Turn> activeTurns = new ArrayList<>(); 

        if(mainLight != null && mainLight.getState() == LightState.GREEN){
            activeTurns.add(Turn.STRAIGHT);
        }
        if(leftTurnArrow != null && leftTurnArrow.getState() == LightState.GREEN){
            activeTurns.add(Turn.LEFT);
        }
        if(rightTurnArrow != null && rightTurnArrow.getState() == LightState.GREEN){
            activeTurns.add(Turn.RIGHT);
        }

        return activeTurns;
    }
}
