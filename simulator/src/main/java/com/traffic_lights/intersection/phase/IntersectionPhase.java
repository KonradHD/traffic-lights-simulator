package com.traffic_lights.intersection.phase;

import com.traffic_lights.model.Direction;
import com.traffic_lights.model.Turn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Data
@AllArgsConstructor
public class IntersectionPhase {

    private final Map<Direction, List<Turn>> paths;
    private final int basicDuration;

    @Setter
    private int optimalDuration;
    private int waitingTime;


    public IntersectionPhase(List<Direction> directions, List<List<Turn>> turns, int basicDuration) {
        if(directions.size() != turns.size()) {
            throw new IllegalArgumentException("Directions and Turns must have the same length");
        }
        paths = new HashMap<>();
        this.basicDuration = basicDuration;

        for(int i = 0; i < directions.size(); i++) {
            paths.put(directions.get(i), turns.get(i));
        }
    }


    public List<Direction> getDirections() {
        return paths.keySet().stream().toList();
    }


    public List<Turn> getTurns(Direction direction) {
        return paths.get(direction);
    }

    public void increaseWaitingTime(){
        this.waitingTime++;
    }

}
