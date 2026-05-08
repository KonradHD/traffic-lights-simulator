package com.traffic_lights.dto.intersection;

/**
 * Data Transfer Object containing the calibration weights for the intersection's scheduling logic.
 * <p>
 * These parameters are used by adaptive scheduling implementations
 * to calculate the priority of different traffic phases. By adjusting these weights in the
 * configuration, the simulation can be tuned to be more responsive to queue lengths or more
 * sensitive to vehicle wait times.
 */
public record IntersectionParameters(
        double weightQueue,
        double weightWaitTime
) {
}
