package com.traffic_lights.components;

import com.traffic_lights.components.intersection.IntersectionPhase;
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

    public static List<IntersectionPhase> createLeftArrowsPhases(){
        List<IntersectionPhase> phases = new ArrayList<>();
        List<Turn> mainLightTurns = List.of(Turn.STRAIGHT, Turn.RIGHT);
        List<Turn> leftTurns = List.of(Turn.LEFT);

        phases.add(
                new IntersectionPhase(
                        List.of(Direction.NORTH, Direction.SOUTH),
                        List.of(mainLightTurns, mainLightTurns),
                        timeConfig.getMainLightPhaseTime()
                )
        );
        phases.add(
                new IntersectionPhase(
                        List.of(Direction.NORTH, Direction.SOUTH),
                        List.of(leftTurns, leftTurns),
                        timeConfig.getLeftTurningPhaseTime()
                )
        );
        phases.add(
                new IntersectionPhase(
                        List.of(Direction.WEST, Direction.EAST),
                        List.of(mainLightTurns, mainLightTurns),
                        timeConfig.getMainLightPhaseTime()
                )
        );
        phases.add(
                new IntersectionPhase(
                        List.of(Direction.WEST, Direction.EAST),
                        List.of(leftTurns, leftTurns),
                        timeConfig.getLeftTurningPhaseTime()
                )
        );
        return phases;
    }

    public static List<IntersectionPhase> createSplitPhases() {
        List<IntersectionPhase> phases = new ArrayList<>();
        List<Turn> allTurns = List.of(Turn.STRAIGHT, Turn.LEFT, Turn.RIGHT);

        phases.add(new IntersectionPhase(
                List.of(Direction.NORTH),
                List.of(allTurns),
                timeConfig.getSplitPhaseTime()
        ));

        phases.add(new IntersectionPhase(
                List.of(Direction.SOUTH),
                List.of(allTurns),
                timeConfig.getSplitPhaseTime()
        ));

        phases.add(new IntersectionPhase(
                List.of(Direction.EAST),
                List.of(allTurns),
                timeConfig.getSplitPhaseTime()
        ));

        phases.add(new IntersectionPhase(
                List.of(Direction.WEST),
                List.of(allTurns),
                timeConfig.getSplitPhaseTime()
        ));
        return phases;
    }

    public static List<IntersectionPhase> createRightTurnArrowsPhases() {
        List<IntersectionPhase> phases = new ArrayList<>();
        List<Turn> rightStraightTurn = List.of(Turn.STRAIGHT, Turn.RIGHT);
        List<Turn> conditionallyRightTurn = List.of(Turn.RIGHT);
        List<Turn> leftTurns = List.of(Turn.LEFT);

        phases.add(
                new IntersectionPhase(
                        List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST),
                        List.of(rightStraightTurn, rightStraightTurn, conditionallyRightTurn, conditionallyRightTurn),
                        timeConfig.getMainLightPhaseTime()
                )
        );
        new IntersectionPhase(
                List.of(Direction.NORTH, Direction.SOUTH),
                List.of(leftTurns, leftTurns),
                timeConfig.getLeftTurningPhaseTime()
        );
        phases.add(
                new IntersectionPhase(
                        List.of(Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH),
                        List.of(rightStraightTurn, rightStraightTurn, conditionallyRightTurn, conditionallyRightTurn),
                        timeConfig.getMainLightPhaseTime()
                )
        );
        phases.add(
                new IntersectionPhase(
                        List.of(Direction.WEST, Direction.EAST),
                        List.of(leftTurns, leftTurns),
                        timeConfig.getLeftTurningPhaseTime()
                )
        );
        return phases;
    }

}
