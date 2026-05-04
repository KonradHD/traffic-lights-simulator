package com.traffic_lights.config;

import java.util.concurrent.ThreadLocalRandom;

import com.traffic_lights.components.Direction;
import com.traffic_lights.components.Turn;

public class StartingPhase {

    public record InitialSignal(Direction direction, Turn turn) {}

    public static InitialSignal generateRandomStart() {
        Direction[] directions = Direction.values();
        Turn[] turns = Turn.values();

        int randomDirectionIndex = ThreadLocalRandom.current().nextInt(directions.length);
        int randomTurnIndex = ThreadLocalRandom.current().nextInt(turns.length);

        Direction randomDirection = directions[randomDirectionIndex];
        Turn randomTurn = turns[randomTurnIndex];
        
        return new InitialSignal(randomDirection, randomTurn);
    }
}
