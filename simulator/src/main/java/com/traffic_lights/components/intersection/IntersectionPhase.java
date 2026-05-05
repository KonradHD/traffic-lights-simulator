package com.traffic_lights.components.intersection;

import com.traffic_lights.components.Direction;
import com.traffic_lights.components.Turn;
import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Data
public class IntersectionPhase {
    private int maxDuration;
    private final Map<Direction, List<Turn>> paths = new HashMap<>();


    public IntersectionPhase(List<Direction> directions, List<List<Turn>> turns, int maxDuration) {
        if(directions.size() != turns.size()) {
            throw new IllegalArgumentException("Directions and Turns must have the same length");
        }
        this.maxDuration = maxDuration;

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

}
