package com.traffic_lights.intersection.phase;

public interface PhaseMetricsProvider {
    int getVehiclesForPhase(IntersectionPhase phase);
    int getVehiclesOverall();
}