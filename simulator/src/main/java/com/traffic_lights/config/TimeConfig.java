package com.traffic_lights.config;

import java.util.EnumMap;
import java.util.Map;

import com.traffic_lights.components.lights.LightState;


public class TimeConfig {
    
    private final Map<LightState, Integer> lightDuration;

    public TimeConfig() {
        this.lightDuration = new EnumMap<>(LightState.class);
        this.lightDuration.put(LightState.RED, 30);
        this.lightDuration.put(LightState.ORANGE, 2);
        this.lightDuration.put(LightState.GREEN, 30);
        this.lightDuration.put(LightState.NONFUNCTIONAL, 0);
    }

    public TimeConfig(Map<LightState, Integer> customDurations) {
        this();
        if (customDurations != null) {
            this.lightDuration.putAll(customDurations); 
        }
    }

    public TimeConfig(int mainLightsDuration) {
        this();
        this.lightDuration.put(LightState.RED, mainLightsDuration);
        this.lightDuration.put(LightState.GREEN, mainLightsDuration);
    }


    public int getDurationFor(LightState state) {
        return lightDuration.getOrDefault(state, 0); 
    }

    public void setDurationFor(LightState state, Integer duration) {
        lightDuration.put(state, duration);
    }
}
