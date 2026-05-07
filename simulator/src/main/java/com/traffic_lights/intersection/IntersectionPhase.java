package com.traffic_lights.intersection;

import com.traffic_lights.model.Direction;
import com.traffic_lights.model.Turn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Data
@AllArgsConstructor
public class IntersectionPhase {

    private final Map<Direction, List<Turn>> paths;
    private int maxDuration;


    public IntersectionPhase(List<Direction> directions, List<List<Turn>> turns, int maxDuration) {
        if(directions.size() != turns.size()) {
            throw new IllegalArgumentException("Directions and Turns must have the same length");
        }
        paths = new HashMap<>();
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
