package com.traffic_lights.intersection.phase;

import java.util.List;

public class HybridPhaseScheduler implements PhaseScheduler {

    private final double weightQueue;
    private final double weightWaitTime;

    public HybridPhaseScheduler(double weightQueue, double weightWaitTime) {
        this.weightQueue = weightQueue;
        this.weightWaitTime = weightWaitTime;
    }

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


    public double calculatePhasePriority(IntersectionPhase phase, PhaseMetricsProvider metricsProvider) {
        int vehiclesCount = metricsProvider.getVehiclesForPhase(phase);
        int waitingTime = phase.getWaitingTime();
        return (this.weightQueue * vehiclesCount) + (this.weightWaitTime * waitingTime);
    }

}