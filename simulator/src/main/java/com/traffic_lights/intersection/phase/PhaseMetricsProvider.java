package com.traffic_lights.intersection.phase;

/**
 * Interface providing real-time traffic statistics and metrics to the phase scheduling logic.
 * <p>
 * This interface decouples the high-level scheduling algorithms from the low-level
 * intersection implementation. It allows schedulers to query current traffic density
 * without needing direct access to lane or vehicle objects.
 * </p>
 */
public interface PhaseMetricsProvider {

    /**
     * Retrieves the number of vehicles currently waiting in all lanes governed
     * by a specific intersection phase.
     * <p>
     * This metric is typically used to determine if a phase should be prioritized
     * or extended based on demand.
     * </p>
     *
     * @param phase The {@link IntersectionPhase} to check for waiting traffic.
     * @return The count of vehicles currently queued in the relevant lanes.
     */
    int getVehiclesForPhase(IntersectionPhase phase);

    /**
     * Retrieves the total number of vehicles currently waiting across the entire
     * intersection, regardless of their direction or lane.
     *
     * @return The total count of waiting vehicles at the intersection.
     */
    int getVehiclesOverall();
}