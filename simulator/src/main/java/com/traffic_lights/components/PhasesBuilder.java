package com.traffic_lights.components;

import com.traffic_lights.config.PhaseTimeConfig;

import java.util.ArrayList;
import java.util.List;

public class PhasesBuilder {

    private static final PhaseTimeConfig timeConfig = new PhaseTimeConfig();

    public static List<IntersectionPhase> createStandardPhases(){
        List<IntersectionPhase> phases = new ArrayList<>();
        List<Turn> turns = List.of(Turn.STRAIGHT, Turn.LEFT, Turn.RIGHT);
        phases.add(
                new IntersectionPhase(
                        List.of(Direction.NORTH, Direction.SOUTH),
                        List.of(turns, turns),
                        timeConfig.getMainLightPhaseTime()
                )
        );
        phases.add(
                new IntersectionPhase(
                        List.of(Direction.WEST, Direction.EAST),
                        List.of(turns, turns),
                        timeConfig.getMainLightPhaseTime()
                )
        );
        return phases;
    }
}
