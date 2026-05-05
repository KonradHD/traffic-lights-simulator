package com.traffic_lights.components.lights;

import com.traffic_lights.components.Turn;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class RoadLights {
    // light for driving straight or right
    private LightActivity mainLight;
    
    // driving left
    private LightActivity leftTurnArrow; 
    
    // conditional driving right
    private LightActivity rightTurnArrow; 


    public void nextStepForAll(){
        if(mainLight != null){
            mainLight.nextStep();
        }

        if(leftTurnArrow != null){
            leftTurnArrow.nextStep();
        }

        if(rightTurnArrow != null){
            rightTurnArrow.nextStep();
        }
    }

    public void applyAllowedTurns(List<Turn> allowedTurns) {
        if (allowedTurns == null) {
            allowedTurns = new ArrayList<>();
        }

        // Główne światło (zwykle odpowiada za jazdę PROSTO, a czasem w PRAWO)
        if (mainLight != null) {
            if (allowedTurns.contains(Turn.STRAIGHT) || (allowedTurns.contains(Turn.RIGHT) && rightTurnArrow == null)) {
                mainLight.changeState(LightState.GREEN);
            } else {
                mainLight.changeState(LightState.RED);
            }
        }

        // Strzałka w lewo
        if (leftTurnArrow != null) {
            if (allowedTurns.contains(Turn.LEFT)) {
                leftTurnArrow.changeState(LightState.GREEN);
            } else {
                leftTurnArrow.changeState(LightState.RED);
            }
        }

        // Strzałka w prawo
        if (rightTurnArrow != null) {
            if (allowedTurns.contains(Turn.RIGHT)) {
                rightTurnArrow.changeState(LightState.GREEN);
            } else {
                rightTurnArrow.changeState(LightState.RED);
            }
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
