package com.traffic_lights.intersection.phase;

import lombok.AllArgsConstructor;

import java.util.List;

/**
 * An adaptive implementation of {@link PhaseScheduler} that uses a weighted hybrid algorithm
 * to determine intersection priorities and phase durations.
 * <p>
 * The hybrid nature of this scheduler comes from its ability to balance two competing
 * traffic management goals:
 * <ul>
 * <li><b>Demand:</b> prioritizing phases with the most waiting vehicles.</li>
 * <li><b>Fairness:</b> preventing phase starvation by prioritizing phases
 * that have been waiting the longest.</li>
 * </ul>
 * </p>
 */
@AllArgsConstructor
public class HybridPhaseScheduler implements PhaseScheduler {

    /** The multiplier applied to the number of waiting vehicles when calculating priority. */
    private final double weightQueue;

    /** The multiplier applied to the phase's accumulated waiting time when calculating priority. */
    private final double weightWaitTime;

    /**
     * Evaluates all inactive phases and selects the one with the highest priority score.
     * <p>
     * The method calculates a priority score for every phase (except the currently active one)
     * using the {@link #calculatePhasePriority(IntersectionPhase, PhaseMetricsProvider)} method.
     * If all calculated priorities are zero, it defaults
     * to a standard Round Robin selection - the next sequential index.
     * </p>
     *
     * @param phases The list of all available intersection phases.
     * @param currentPhaseIndex The index of the currently active phase.
     * @param metricsProvider The provider for real-time intersection metrics.
     * @return The list index of the phase that should be activated next.
     */
    @Override
    public int determineNextPhaseIndex(List<IntersectionPhase> phases, int currentPhaseIndex, PhaseMetricsProvider metricsProvider) {
        int bestPhaseIndex = (currentPhaseIndex + 1) % phases.size();
        double maxPriority = 0.0;

        for (int i = 0; i < phases.size(); i++) {
            if (i == currentPhaseIndex) continue;
            IntersectionPhase phase = phases.get(i);
            double phasePriority = calculatePhasePriority(phase, metricsProvider);

            if (phasePriority > maxPriority) {
                maxPriority = phasePriority;
                bestPhaseIndex = i;
            }
        }
        return bestPhaseIndex;
    }

    /**
     * Calculates a dynamic duration for a phase based on its proportional share of total traffic.
     * <p>
     * The formula allocates time from a base cycle duration proportionally to the
     * percentage of total vehicles waiting for this specific phase.
     * Boundary conditions are handled as follows:
     * <ul>
     * <li>If there are zero vehicles globally, it falls back to the phase's {@code basicDuration}.</li>
     * <li>If the phase has vehicles but its proportional time rounds down to 0, it is guaranteed
     * at least 1 simulation step to prevent complete stalling.</li>
     * <li>If the phase has exactly 0 vehicles, it is skipped entirely.</li>
     * </ul>
     * </p>
     *
     * @param phase The phase being evaluated.
     * @param metricsProvider The provider for real-time intersection metrics.
     * @param cycleBasicDuration The theoretical total duration of a complete cycle, used as a baseline.
     * @return The calculated number of simulation steps this phase should remain active.
     */
    @Override
    public int calculateOptimalPhaseTime(IntersectionPhase phase, PhaseMetricsProvider metricsProvider, int cycleBasicDuration) {
        int vehiclesInPhase = metricsProvider.getVehiclesForPhase(phase);
        int vehiclesOverall = metricsProvider.getVehiclesOverall();

        if (vehiclesOverall == 0) {
            return phase.getBasicDuration();
        }

        double vehicleProportion = (double) vehiclesInPhase / vehiclesOverall;
        int optimalTime = (int) Math.round(vehicleProportion * cycleBasicDuration);

        return vehiclesInPhase > 0 ? Math.max(1, optimalTime) : 0;
    }

    /**
     * Calculates the raw priority score for a specific phase using a linear combination formula.
     * <p>
     * Formula: {@code (weightQueue * vehiclesCount) + (weightWaitTime * waitingTime)}
     * </p>
     *
     * @param phase The intersection phase to evaluate.
     * @param metricsProvider The provider used to fetch the current vehicle count for the phase.
     * @return A {@code double} value representing the urgency of activating this phase.
     */
    public double calculatePhasePriority(IntersectionPhase phase, PhaseMetricsProvider metricsProvider) {
        int vehiclesCount = metricsProvider.getVehiclesForPhase(phase);
        int waitingTime = phase.getWaitingTime();
        return (this.weightQueue * vehiclesCount) + (this.weightWaitTime * waitingTime);
    }

}