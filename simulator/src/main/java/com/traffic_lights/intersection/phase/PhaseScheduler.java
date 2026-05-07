package com.traffic_lights.intersection.phase;

import java.util.List;

public interface PhaseScheduler {
    int determineNextPhaseIndex(List<IntersectionPhase> phases, int currentPhaseIndex, PhaseMetricsProvider metricsProvider);
    int calculateOptimalPhaseTime(IntersectionPhase phase, PhaseMetricsProvider metricsProvider, int cycleBasicDuration);
}
