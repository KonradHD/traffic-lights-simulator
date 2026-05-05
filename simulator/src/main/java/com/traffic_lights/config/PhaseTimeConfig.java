package com.traffic_lights.config;

import lombok.Getter;

@Getter
public class PhaseTimeConfig {
    private final int mainLightPhaseTime = 30;
    private final int leftTurningPhaseTime = 10;
}
