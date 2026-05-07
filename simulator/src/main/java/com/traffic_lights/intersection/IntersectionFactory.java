package com.traffic_lights.intersection;

import com.traffic_lights.config.IntersectionConfig;
import com.traffic_lights.dto.intersection.IntersectionLayout;
import com.traffic_lights.dto.intersection.IntersectionParameters;
import com.traffic_lights.intersection.phase.HybridPhaseScheduler;
import com.traffic_lights.intersection.phase.IntersectionPhase;
import com.traffic_lights.intersection.phase.PhaseScheduler;

import java.util.List;

public class IntersectionFactory {

    public static Intersection createIntersection(String intersectionStyle, String intersectionType) {
        IntersectionConfig.loadConfig();
        IntersectionLayout layoutTemplate = IntersectionConfig.getLayoutForType(intersectionType.toUpperCase());
        IntersectionParameters parameters = IntersectionConfig.getParameters();
        PhaseScheduler scheduler = new HybridPhaseScheduler(parameters.weightQueue(), parameters.weightWaitTime());

        List<IntersectionPhase> phases = layoutTemplate.phases().stream()
                .map(dto -> new IntersectionPhase(dto.paths(), dto.basicDuration(), dto.basicDuration(), 0))
                .toList();

        return switch (intersectionStyle.toUpperCase()) {
            case "SINGLE" -> new SingleLaneIntersection(intersectionType, phases, scheduler);
            case "MULTI" -> new MultiLaneIntersection(intersectionType, phases, scheduler);
            default -> throw new IllegalArgumentException("Unknown intersection style provided: " + intersectionStyle);
        };
    }
}
