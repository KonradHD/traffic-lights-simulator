package com.traffic_lights.intersection;

import com.traffic_lights.intersection.phase.HybridPhaseScheduler;
import com.traffic_lights.intersection.phase.IntersectionPhase;
import com.traffic_lights.intersection.phase.PhaseMetricsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HybridPhaseSchedulerTest {

    private PhaseMetricsProvider mockProvider;
    private IntersectionPhase phase0;
    private IntersectionPhase phase1;
    private IntersectionPhase phase2;
    private List<IntersectionPhase> phases;

    private double WEIGHT_QUEUE;
    private double WEIGHT_WAIT;
    private HybridPhaseScheduler scheduler;

    @BeforeEach
    void setUp() {
        WEIGHT_QUEUE = 1.5;
        WEIGHT_WAIT = 2.0;
        mockProvider = mock(PhaseMetricsProvider.class);

        phase0 = mock(IntersectionPhase.class);
        phase1 = mock(IntersectionPhase.class);
        phase2 = mock(IntersectionPhase.class);
        phases = List.of(phase0, phase1, phase2);

        scheduler = new HybridPhaseScheduler(WEIGHT_QUEUE, WEIGHT_WAIT);
    }


    @Test
    void shouldCalculateCorrectPriority() {
        when(mockProvider.getVehiclesForPhase(phase1)).thenReturn(10);
        when(phase1.getWaitingTime()).thenReturn(5);
        double priority = scheduler.calculatePhasePriority(phase1, mockProvider);

        assertEquals(25.0, priority, 0.001);
    }


    @Test
    void shouldDetermineNextPhaseHighestPriority() {
        when(mockProvider.getVehiclesForPhase(phase1)).thenReturn(2);
        when(phase1.getWaitingTime()).thenReturn(20);

        when(mockProvider.getVehiclesForPhase(phase2)).thenReturn(40);
        when(phase2.getWaitingTime()).thenReturn(0);

        int nextPhaseIndex = scheduler.determineNextPhaseIndex(phases, 0, mockProvider);

        assertEquals(2, nextPhaseIndex);
    }

    @Test
    void shouldFallbackToRoundRobin() {
        int nextPhaseIndex = scheduler.determineNextPhaseIndex(phases, 1, mockProvider);

        assertEquals(2, nextPhaseIndex);
    }


    @Test
    void shouldReturnBasicDuration() {
        when(mockProvider.getVehiclesOverall()).thenReturn(0);
        when(phase1.getBasicDuration()).thenReturn(15);

        int optimalTime = scheduler.calculateOptimalPhaseTime(phase1, mockProvider, 60);

        assertEquals(15, optimalTime);
    }

    @Test
    void shouldCalculateProportionalTime() {
        when(mockProvider.getVehiclesOverall()).thenReturn(100);
        when(mockProvider.getVehiclesForPhase(phase1)).thenReturn(25);

        int optimalTime = scheduler.calculateOptimalPhaseTime(phase1, mockProvider, 60);

        assertEquals(15, optimalTime);
    }

    @Test
    void shouldReturnZeroNoVehiclesInPhase() {
        when(mockProvider.getVehiclesOverall()).thenReturn(100);
        when(mockProvider.getVehiclesForPhase(phase1)).thenReturn(0);

        int optimalTime = scheduler.calculateOptimalPhaseTime(phase1, mockProvider, 60);

        assertEquals(0, optimalTime);
    }

    @Test
    void shouldForceMinimumTime() {
        when(mockProvider.getVehiclesOverall()).thenReturn(1000);
        when(mockProvider.getVehiclesForPhase(phase1)).thenReturn(1);

        int optimalTime = scheduler.calculateOptimalPhaseTime(phase1, mockProvider, 60);

        assertEquals(1, optimalTime);
    }
}