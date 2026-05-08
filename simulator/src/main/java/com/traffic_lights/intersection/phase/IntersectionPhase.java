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

/**
 * Represents a specific state configuration within an intersection's cycle.
 * <p>
 * An {@code IntersectionPhase} defines which movements are permitted from
 * which approach directions during a specific period of time. It acts as a container
 * for the traffic signal logic, holding both static timing configurations and
 * dynamic metrics like waiting time.
 * </p>
 */
@Getter
@Data
@AllArgsConstructor
public class IntersectionPhase {

    /** * A mapping of approach {@link Direction}s to the list of permitted {@link Turn}s
     * allowed during this phase.
     */
    private final Map<Direction, List<Turn>> paths;

    /** The default duration for this phase as defined in the config. */
    private final int basicDuration;

    /** * The dynamically calculated duration for the current cycle.
     */
    @Setter
    private int optimalDuration;

    /** Tracks how long this phase has been waiting to become active. */
    private int waitingTime;

    /**
     * Constructs a new {@code IntersectionPhase} by mapping directions to their permitted turns.
     *
     * @param directions    List of entry directions that will receive a green light.
     * @param turns         A corresponding list of turn lists permitted for each direction.
     * @param basicDuration The standard time slice for this phase.
     * @throws IllegalArgumentException if the size of directions does not match the size of turns.
     */
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

    /**
     * Retrieves all entry directions that are active during this phase.
     *
     * @return A {@link List} of {@link Direction} keys present in the phase mapping.
     */
    public List<Direction> getDirections() {
        return paths.keySet().stream().toList();
    }

    /**
     * Retrieves the specific maneuvers allowed for a given direction during this phase.
     *
     * @param direction The approach direction to query.
     * @return A {@link List} of {@link Turn}s allowed, or {@code null} if the direction is not part of this phase.
     */
    public List<Turn> getTurns(Direction direction) {
        return paths.get(direction);
    }

    /**
     * Increments the waiting time counter for this phase.
     * Typically called during every simulation step where this phase is not active and there is at least one vehicle waiting.
     */
    public void increaseWaitingTime(){
        this.waitingTime++;
    }

}
