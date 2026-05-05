package com.traffic_lights.components.lights;

import static com.traffic_lights.exception.UndeclaredNextStateException.undeclaredNextStateException;

public enum LightState {
    RED,
    ORANGE,
    GREEN,
    NONFUNCTIONAL;

    public LightState nextState(){
        if(null != this){
            switch (this) {
                case NONFUNCTIONAL -> throw undeclaredNextStateException(NONFUNCTIONAL.toString());
                case RED -> {
                    return GREEN;
                }
                case GREEN -> {
                    return ORANGE;
                }
                case ORANGE -> {
                    return RED;
                }
                default -> {
                }
            }
        }
        throw new IllegalStateException("Unknown light state: " + this);
    }
}
