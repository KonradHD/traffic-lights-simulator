package com.traffic_lights.components.intersection;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.traffic_lights.components.Direction.*;
import static com.traffic_lights.components.lights.LightState.*;

import com.traffic_lights.components.Direction;
import com.traffic_lights.components.lights.LightActivity;
import com.traffic_lights.components.lights.LightState;
import com.traffic_lights.components.lights.RoadLights;
import com.traffic_lights.config.TimeConfig;

import lombok.Getter;


 @Getter
// @AllArgsConstructor
public class IntersectionType {

    private String typeName;
    private Map<Direction, RoadLights> roadsConfig;


    public IntersectionType(String typeName, Map<Direction, RoadLights> roadsConfig){
        this.typeName = typeName;

        this.roadsConfig = roadsConfig.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (existing, replacement) -> existing,
                () -> new EnumMap<>(Direction.class)
            ));
    }

    public static IntersectionType createStandard() {
        Map<Direction, RoadLights> standardRoads = Map.of(
            NORTH, new RoadLights(new LightActivity(RED, new TimeConfig()), null, null),
            SOUTH, new RoadLights(new LightActivity(RED, new TimeConfig()), null, null),
            EAST,  new RoadLights(new LightActivity(RED, new TimeConfig()), null, null),
            WEST,  new RoadLights(new LightActivity(RED, new TimeConfig()), null, null)
        );
        return new IntersectionType("STANDARD", standardRoads);
    }

    // --- TYP 2: Skrzyżowanie z bezkolizyjnymi lewoskrętami ---
    public static IntersectionType createWithLeftTurnArrows() {
        Map<Direction, RoadLights> advancedRoads = Map.of(
            NORTH, new RoadLights(new LightActivity(LightState.RED, new TimeConfig()), new LightActivity(LightState.RED, new TimeConfig(15)), null),
            SOUTH, new RoadLights(new LightActivity(LightState.RED, new TimeConfig()), new LightActivity(LightState.RED, new TimeConfig(15)), null),
            EAST,  new RoadLights(new LightActivity(LightState.RED, new TimeConfig()), new LightActivity(LightState.RED, new TimeConfig(15)), null),
            WEST,  new RoadLights(new LightActivity(LightState.RED, new TimeConfig()), new LightActivity(LightState.RED, new TimeConfig(15)), null)
        );
        return new IntersectionType("LEFT_TURN_ARROWS", advancedRoads);
    }

    // --- TYP 3: Skrzyżowanie z warunkowymi strzałkami w prawo ---
    public static IntersectionType createWithRightTurnArrows() {
        Map<Direction, RoadLights> rightTurnRoads = Map.of(
            NORTH, new RoadLights(new LightActivity(RED, new TimeConfig()), null, new LightActivity(RED, new TimeConfig())),
            SOUTH, new RoadLights(new LightActivity(RED, new TimeConfig()), null, new LightActivity(RED, new TimeConfig())),
            EAST,  new RoadLights(new LightActivity(RED, new TimeConfig()), null, new LightActivity(RED, new TimeConfig())),
            WEST,  new RoadLights(new LightActivity(RED, new TimeConfig()), null, new LightActivity(RED, new TimeConfig()))
        );
        return new IntersectionType("RIGHT_TURN_ARROWS", rightTurnRoads);
    }

    public static IntersectionType createSplitPhases(){
        IntersectionType standard = createStandard();
        return new IntersectionType("SPLIT_PHASES", standard.getRoadsConfig());
    }
}
