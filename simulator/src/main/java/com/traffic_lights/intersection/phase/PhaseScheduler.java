package com.traffic_lights.intersection.phase;

import java.util.List;

/**
 * Strategy interface for managing the timing and sequencing of intersection phases.
 * <p>
 * Implementations of this interface define the adaptability of the traffic controller.
 * This can range from simple fixed-time cycles to complex, adaptive algorithms that
 * respond to real-time traffic density and waiting times.
 * </p>
 */
public interface PhaseScheduler {

    /**
     * Determines which phase should be activated next in the simulation sequence.
     * <p>
     * While a standard scheduler might simply increment the index,
     * an adaptive scheduler may skip phases with no waiting vehicles or prioritize
     * heavily congested roads.
     * </p>
     *
     * @param phases The list of all available phases for the intersection.
     * @param currentPhaseIndex The index of the phase that is currently active.
     * @param metricsProvider A provider used to query current traffic conditions.
     * @return The index of the next {@link IntersectionPhase} to be activated.
     */
    int determineNextPhaseIndex(List<IntersectionPhase> phases, int currentPhaseIndex, PhaseMetricsProvider metricsProvider);

    /**
     * Calculates the duration (in simulation steps) for which a specific phase
     * should remain active.
     * <p>
     * This allows for dynamic light switching or extending the green light duration
     * if a high volume of vehicles is detected on the prioritized roads.
     * </p>
     *
     * @param phase The phase for which the duration is being calculated.
     * @param metricsProvider A provider used to query real-time traffic data.
     * @param cycleBasicDuration The base duration of the whole light cycle defined in the global configuration,
     * as the sum of individual phases.
     * @return The number of simulation steps the phase should stay active.
     */
    int calculateOptimalPhaseTime(IntersectionPhase phase, PhaseMetricsProvider metricsProvider, int cycleBasicDuration);
}
