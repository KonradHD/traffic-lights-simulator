package com.traffic_lights.intersection;

import com.traffic_lights.config.IntersectionConfig;
import com.traffic_lights.dto.intersection.IntersectionLayout;
import com.traffic_lights.dto.intersection.IntersectionParameters;
import com.traffic_lights.intersection.phase.HybridPhaseScheduler;
import com.traffic_lights.intersection.phase.IntersectionPhase;
import com.traffic_lights.intersection.phase.PhaseScheduler;

import java.util.List;

/**
 * Factory class responsible for the initialization and configuration of intersection objects.
 * <p>
 * The {@code IntersectionFactory} serves as a Creational Pattern implementation that
 * abstracts the complexity of building an {@link Intersection}. It handles the
 * loading of global configurations, the mapping of DTO templates to domain phases,
 * and the selection of the appropriate intersection subclass based on the provided style.
 * </p>
 */
public class IntersectionFactory {

    /**
     * Creates and initializes a concrete {@link Intersection} instance based on style and type.
     * <p>
     * The creation process involves:
     * <ol>
     * <li>Loading the system configuration via {@link IntersectionConfig}.</li>
     * <li>Retrieving the specific {@link IntersectionLayout} for the requested type.</li>
     * <li>Instantiating a {@link HybridPhaseScheduler} using configured weights.</li>
     * <li>Transforming phase DTOs into domain-level {@link IntersectionPhase} objects.</li>
     * <li>Returning either a {@link SingleLaneIntersection} or {@link MultiLaneIntersection}.</li>
     * </ol>
     * </p>
     *
     * @param intersectionStyle The architectural style of the intersection.
     * @param intersectionType  The specific configuration name
     * found in the JSON config.
     * @return A fully initialized {@link Intersection} implementation.
     * @throws IllegalArgumentException if an unsupported intersection style is provided or
     * if the configuration for the type is missing.
     */
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
